# Spring MVC with Embedded Tomcat (Without Spring Boot)

> **Deconstructing the Magic:** A bare-metal, zero-Boot Java implementation of Spring MVC running inside an embedded Apache Tomcat container.

This project strips away Spring Boot's auto-configuration layer to expose the low-level orchestration required to bootstrap a enterprise Spring web application from scratch—manually wiring application contexts, web servers, servlet containers, and request-handling infrastructure.

---

## What Is Handled Manually

In a standard Spring Boot application, `spring-boot-starter-web` configures server initialization and framework wiring under the hood. Here, every component is constructed explicitly:

| Component | Manual Implementation in This Project |
| --- | --- |
| **Embedded Server** | Programmatically instantiated and configured via `new Tomcat()` and custom connectors. |
| **Context Hierarchy** | Explicit creation of Root (`AnnotationConfigApplicationContext`) and Web (`AnnotationConfigWebApplicationContext`) contexts. |
| **Front Controller** | `DispatcherServlet` instantiated manually via `new DispatcherServlet(webContext)` and bound to Tomcat. |
| **Servlet Mapping** | Wildcard route `/` mapped directly to `DispatcherServlet` within Tomcat's context. |
| **Component Scanning** | Declaratively isolated using `@ComponentScan` in dedicated configuration classes (`AppConfig` & `WebConfig`). |
| **MVC Infrastructure** | Bootstrapped explicitly by applying `@EnableWebMvc`. |

---

## Context Architecture: Root vs. Web Context

Spring MVC strictly enforces a **Parent-Child ApplicationContext hierarchy** to maintain clean layer separation:

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

* **Root Application Context (`AppConfig`):** Manages non-web singletons, business logic (`@Service`), and data access components (`@Repository`).
* **Web Application Context (`WebConfig`):** Manages presentation components (`@Controller`) and references the Root Context as its parent.
* **Lookup Search Rules:** If a dependency is missing in the Web Context, Spring delegates lookup upward to the Root Context. *Root beans cannot look down into the Web Context.*

---

## Startup Pipeline

```
 [ main() ]
     │
     ├───► 1. Instantiate Root ApplicationContext (AppConfig)
     │       └── Scan @Service & @Repository ──► Instantiate Business Beans
     │
     ├───► 2. Instantiate Web ApplicationContext (WebConfig)
     │       ├── Scan @Controller
     │       └── Set Parent Context = Root Context
     │
     ├───► 3. Instantiate DispatcherServlet
     │       └── Pass Web ApplicationContext to DispatcherServlet
     │
     ├───► 4. Bootstrap Embedded Tomcat
     │       ├── Create Tomcat instance & HTTP Connector
     │       ├── Initialize Tomcat Context
     │       ├── Register DispatcherServlet
     │       └── Map "/" ──► DispatcherServlet
     │
     └───► 5. Start Server & Initialize Servlet
             └── DispatcherServlet.init() loads & caches MVC infrastructure

```

---

## What `@EnableWebMvc` Actually Does

Annotating `WebConfig` with `@EnableWebMvc` imports `DelegatingWebMvcConfiguration`, transforming a standard Spring core context into a full web container by auto-registering these essential infrastructure beans:

* **`RequestMappingHandlerMapping`**: Scans `@Controller` classes and builds the in-memory URL-to-method routing table.
* **`RequestMappingHandlerAdapter`**: Executes controller methods, handling method argument resolution (`@PathVariable`, `@RequestBody`, `@RequestParam`) and return processing.
* **`HttpMessageConverters`**: Registers converters, including `MappingJackson2HttpMessageConverter` for JSON serialization.
* **`FormattingConversionService` & `Validator**`: Drives type conversion, formatting, and payload validation.
* **`HandlerExceptionResolver`**: Catches and formats exceptions thrown during request execution into HTTP responses.

---

## DispatcherServlet & Internal Caching

When Tomcat starts and invokes `DispatcherServlet.init()`, the Front Controller resolves and **caches** all infrastructure components once. They are never looked up on a per-request basis.

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
[ HTTP Request ] (e.g., GET /onepiece/get/luffy)
       │
       ▼
[ Embedded Tomcat Connector ]
       │
       ▼
[ DispatcherServlet ] (Front Controller)
       │
       ├───► 1. Query RequestMappingHandlerMapping (Retrieves HandlerMethod)
       │
       ├───► 2. Delegate execution to RequestMappingHandlerAdapter
       │       │
       │       ├───► Resolve parameters (@PathVariable, @RequestBody via Jackson)
       │       ├───► Execute Controller ──► Service ──► Repository
       │       └───► Capture Java object returned by Controller
       │
       └───► 3. Convert Object to JSON via HttpMessageConverter (Jackson / @ResponseBody)
       │
       ▼
[ HTTP 200 Response (JSON Payload) ]

```

---

## Manual Setup vs. Spring Boot

| Architectural Task | Manual Setup (This Demo) | Spring Boot (`spring-boot-starter-web`) |
| --- | --- | --- |
| **Server Lifecycle** | `new Tomcat().start()` | Handled by `SpringApplication.run()` |
| **Context Wiring** | Programmatically linked hierarchy | Automatically wired via auto-configurations |
| **Servlet Registration** | `Tomcat.addServlet()` & URL mapping | `DispatcherServletAutoConfiguration` |
| **Web MVC Config** | Manual `@EnableWebMvc` annotation | Implicitly included via starters |
| **Dependency Management** | Explicit low-level JAR declarations | Unified versioning via Spring Boot BOM |

---

## The Evolution: From Java Servlets to Spring Web MVC

Understanding why Spring Web MVC exists requires looking at the friction points of traditional Java Servlet development.

```
┌─────────────────────────────────────────┐
│         Traditional Java Servlets       │
│  (1 Servlet = 1 Route/Entity Nightmare) │
└────────────────────┬────────────────────┘
                     │
                     ▼ Evolution
┌─────────────────────────────────────────┐
│            Spring Web MVC               │
│ (1 Front Controller + Annotation Router)│
└─────────────────────────────────────────┘

```

### 1. Traditional Java Servlets (`HttpServlet`)

In classical Java EE web development, every URL pattern required a dedicated `HttpServlet` registered inside `web.xml` (or via `@WebServlet`):

* **Boilerplate Explosion:** Every servlet required manual override of `doGet()`, `doPost()`, `doPut()`, and `doDelete()`.
* **Manual Parameter Extraction:** Developers had to parse query parameters, headers, and path strings manually using `request.getParameter()` and perform explicit data type conversions.
* **Manual Serialization:** Converting incoming JSON required manually reading the `BufferedReader` from `HttpServletRequest`, passing it to a JSON parser, and writing strings back out to `HttpServletResponse.getWriter()`.
* **Tight Coupling:** Code was tightly bound to the Servlet API (`HttpServletRequest`, `HttpServletResponse`), making unit testing difficult without complex mock server environments.

```java
// The Old Way: Servlet Handling
@WebServlet("/user")
public class UserServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = req.getParameter("id");
        User user = userService.find(Long.parseLong(id));
        
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        out.print(new Gson().toJson(user)); // Manual JSON encoding
    }
}

```

### 2. The Spring Web MVC Paradigm Shift

Spring MVC introduced the **Front Controller Design Pattern** through `DispatcherServlet`, revolutionizing Java web development:

* **Single Entry Point:** Instead of dozens of servlets, a single `DispatcherServlet` intercepts **all** incoming traffic (`/`).
* **Declarative Mapping:** Servlets were replaced by standard POJOs annotated with `@Controller` and `@GetMapping`/`@PostMapping`.
* **Automated Data Binding:** Method signatures declaratively request `@PathVariable`, `@RequestParam`, or `@RequestBody`, and `HandlerAdapter` automatically handles type conversion and Jackson deserialization.
* **Decoupled Architecture:** Controllers return domain objects or data models rather than manipulating raw response streams. Spring handles content negotiation and HTTP status mapping seamlessly.

```java
// The Spring MVC Way: Clean, Decoupled POJO
@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.find(id); // Framework handles routing, parsing, and JSON conversion
    }
}

```