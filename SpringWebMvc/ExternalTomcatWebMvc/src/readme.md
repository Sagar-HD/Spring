# Spring MVC with External Tomcat (Without Spring Boot)

> **Deconstructing the WAR Deployment:** A pure Spring MVC web application packaged as a `.war` archive and deployed to a standalone, external Apache Tomcat server—built entirely **without Spring Boot**.

This project exposes how an external Servlet container discovers, bootstraps, and wires a Spring application using the Servlet 3.0+ Specification, replacing legacy `web.xml` deployment descriptors with programmatic Java configuration.

---

## What Is Handled Manually

In Spring Boot, an embedded server starts your application from a `main()` method. With external Tomcat, the server owns the process lifecycle, and your application is a passive component loaded by the container:

| Architectural Task | External Tomcat Implementation |
| --- | --- |
| **Packaging Unit** | Built as a Web Application Archive (`.war`) deployed to Tomcat's `webapps/` directory. |
| **Container Discovery** | Discovered dynamically via the Servlet 3.0 SPI (`ServletContainerInitializer`). |
| **Servlet Registration** | Programmatically registered using `WebApplicationInitializer` instead of `web.xml`. |
| **Context Hierarchy** | Explicit creation of Root (`AppConfig`) and Web (`WebConfig`) `ApplicationContext` instances. |
| **MVC Bootstrapping** | Framework beans wired explicitly by applying `@EnableWebMvc`. |

---

## How External Tomcat Discovers Spring

Without a `main()` method or a `web.xml` file, Tomcat uses the **Servlet 3.0+ Service Provider Interface (SPI)** mechanism to locate and start Spring:

```
[ Tomcat Starts ]
       │
       ▼
[ Scans webapps/anime.war ]
       │
       ▼
[ Inspects META-INF/services/jakarta.servlet.ServletContainerInitializer ]
       │
       ▼
[ Locates SpringServletContainerInitializer ]
       │
       ▼
[ Reads @HandlesTypes(WebApplicationInitializer.class) ]
       │
       ▼
[ Scans Classpath for WebApplicationInitializer Implementations ]
       │
       ▼
[ Instantiates MyWebInitializer (AbstractAnnotationConfigDispatcherServletInitializer) ]
       │
       ▼
[ Calls onStartup() ──► Bootstraps Application Contexts & Registers DispatcherServlet ]

```

---

## Context Architecture: Root vs. Web Context

Spring MVC enforces a strict **Parent-Child ApplicationContext hierarchy** to insulate business logic from web-layer concerns:

```
                      ┌────────────────────────────────────────┐
                      │        Root ApplicationContext         │
                      │ (AnnotationConfigApplicationContext)   │
                      │                                        │
                      │   Contains: @Service, @Repository      │
                      └──────────────────┬─────────────────────┘
                                         │
                                Parent / Child Link
                                         │
                      ┌──────────────────▼─────────────────────┐
                      │        Web ApplicationContext          │
                      │(AnnotationConfigWebApplicationContext)  │
                      │                                        │
                      │   Contains: @Controller, Web Beans     │
                      └────────────────────────────────────────┘

```

* **Root Application Context (`AppConfig`):** Instantiates shared non-web singletons, service components (`@Service`), and data access layers (`@Repository`).
* **Web Application Context (`WebConfig`):** Manages presentation components (`@Controller`), view resolvers, and web infrastructure.
* **Parent-Child Delegation:** When resolving dependencies, `WebApplicationContext` searches internally first. If a bean is missing, it delegates up to `Root ApplicationContext`. Root beans cannot access beans inside Web Context.

---

## Startup Pipeline

```
 [ External Tomcat Boot Sequence ]
     │
     ├───► 1. Tomcat extracts anime.war into webapps/ context
     │
     ├───► 2. SPI triggers SpringServletContainerInitializer
     │
     ├───► 3. Executes MyWebInitializer.onStartup()
     │       │
     │       ├───► Create Root Context (AppConfig) ──► Instantiate Services & Repositories
     │       │
     │       ├───► Create Web Context (WebConfig) ──► Instantiate Controllers
     │       │
     │       └───► Set Parent Context: Web Context ──► Root Context
     │
     ├───► 4. Instantiate DispatcherServlet with Web ApplicationContext
     │
     ├───► 5. Register DispatcherServlet into Tomcat's ServletContext
     │       └── Add Servlet Mapping: "/" ──► DispatcherServlet
     │
     └───► 6. Tomcat initializes DispatcherServlet (init())
             └── Loads & caches HandlerMappings, Adapters, and Converters

```

---

## What `@EnableWebMvc` Actually Does

Annotating `WebConfig` with `@EnableWebMvc` imports `DelegatingWebMvcConfiguration`, which registers the core infrastructure beans required to process HTTP traffic:

* **`RequestMappingHandlerMapping`**: Scans `@Controller` methods and maintains the in-memory URL-to-handler registry.
* **`RequestMappingHandlerAdapter`**: Executes controller methods, handling method parameter resolution (`@PathVariable`, `@RequestBody`, `@RequestParam`) and return processing.
* **`HttpMessageConverters`**: Registers default payload converters, including `MappingJackson2HttpMessageConverter` for JSON processing.
* **`FormattingConversionService` & `Validator**`: Powers data binding, field formatting, and payload validation.
* **`HandlerExceptionResolver`**: Intercepts unhandled controller exceptions and converts them into structured HTTP responses.

---

## DispatcherServlet & Infrastructure Caching

When Tomcat invokes `DispatcherServlet.init()`, the Front Controller resolves and **caches** all infrastructure dependencies once during startup. They are never re-created on individual requests.

```
DispatcherServlet.init()
       │
       ├───► Load & Cache HandlerMappings
       ├───► Load & Cache HandlerAdapters
       ├───► Load & Cache HttpMessageConverters (Jackson / ObjectMapper)
       ├───► Load & Cache HandlerExceptionResolvers
       └───► Load & Cache ViewResolvers / LocaleResolvers / FlashMapManager

```

---

## Request Execution Lifecycle

```
[ HTTP Request ] (e.g., POST /anime/create)
       │
       ▼
[ Standalone Tomcat Engine ]
       │
       ▼
[ DispatcherServlet ] (Front Controller)
       │
       ├───► 1. Query RequestMappingHandlerMapping (Finds matching HandlerMethod)
       │
       ├───► 2. Delegate execution to RequestMappingHandlerAdapter
       │       │
       │       ├───► Deserializes JSON via HttpMessageConverter (Jackson) ──► @RequestBody
       │       ├───► Executes Controller ──► Calls Service ──► Calls Repository
       │       └───► Receives domain object returned by Controller
       │
       └───► 3. Serialize Return Object to JSON via HttpMessageConverter (@ResponseBody)
       │
       ▼
[ HTTP 200/201 Response (JSON Payload) ]

```

---

## Architecture Comparison: Embedded vs. External Tomcat

| Feature | Embedded Tomcat (Programmatic) | External Tomcat (WAR Deployment) |
| --- | --- | --- |
| **Control Authority** | Application controls Server (`main()` starts Tomcat) | Server controls Application (Tomcat loads WAR) |
| **Artifact Format** | Standalone Executable JAR (`.jar`) | Web Application Archive (`.war`) |
| **Entry Point** | `public static void main(String[] args)` | `ServletContainerInitializer` / `WebApplicationInitializer` |
| **Servlet Wiring** | Manual `Tomcat.addServlet()` calls | Container `ServletContext.addServlet()` registration |
| **Server Lifecycle** | Bound directly to application process | Independent container hosting multiple webapps |

---

## The Evolution: `web.xml` to `WebApplicationInitializer`

Before the Servlet 3.0 specification, every Java web application required a `WEB-INF/web.xml` file to define servlets, context parameters, and listeners.

### 1. Legacy Deployment Descriptor (`web.xml`)

```xml
<!-- The Old Way: Verbose XML Declarations -->
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee" version="3.1">
    
    <!-- Root Context Listener -->
    <listener>
        <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
    </listener>

    <!-- DispatcherServlet Declaration -->
    <servlet>
        <servlet-name>dispatcher</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>/WEB-INF/spring/web-context.xml</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>

    <servlet-mapping>
        <servlet-name>dispatcher</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>
</web-app>

```

### 2. Modern Programmatic Initializer (`MyWebInitializer`)

Spring's `AbstractAnnotationConfigDispatcherServletInitializer` encapsulates the entire XML configuration above into type-safe Java code:

```java
// The Modern Way: Type-safe, Zero-XML Java Configuration
public class MyWebInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[] { AppConfig.class }; // Replaces ContextLoaderListener
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] { WebConfig.class }; // Replaces DispatcherServlet XML init-param
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" }; // Replaces <servlet-mapping>
    }
}

```