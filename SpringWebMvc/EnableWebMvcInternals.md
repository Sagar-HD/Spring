# 🌱 Spring MVC Without Spring Boot — `@EnableWebMvc` & `WebMvcConfigurer` Deep Dive

## 🎯 Goal

When building a Spring MVC application **without Spring Boot**, we must manually configure the web framework.

A common question is:

> **What exactly does `@EnableWebMvc` do?**

and

> **Why do we implement `WebMvcConfigurer`?**

Understanding these two concepts removes most of the "magic" behind Spring MVC.

---

# Before Spring MVC

Suppose a browser sends:

```http
GET /api/v1/movies
```

A framework has to answer many questions:

* Which controller should handle this URL?
* Which method should be called?
* How should JSON be converted into Java objects?
* How should Java objects become JSON?
* How should validation work?
* How should exceptions be handled?
* How should static files be served?
* How should type conversion work?
* How should CORS work?

Without Spring MVC, **we would have to build all of this ourselves.**

---

# What `@EnableWebMvc` Actually Does

Think of it as telling Spring:

> **"Please build the complete Spring MVC framework inside my application."**

Without it:

```text
Spring IoC Container
        │
        ▼
Beans only
```

With it:

```text
Spring IoC Container
        │
        ▼
Complete MVC Framework
```

It registers many internal infrastructure beans required for Spring MVC.

---

# MVC Components Created by `@EnableWebMvc`

## 1. DispatcherServlet Support ⭐⭐⭐⭐⭐

Purpose:

Acts as the **Front Controller**.

Flow:

```
Browser
    │
    ▼
DispatcherServlet
    │
    ▼
Controller
```

Without it:

Spring has controllers but no central request dispatcher.

---

## 2. RequestMappingHandlerMapping

Purpose:

Finds which controller method should handle a request.

Example:

```java
@GetMapping("/movies")
```

Internally it stores something like

```
GET /movies
↓

MovieController#getMovies()
```

Without it:

Spring has no idea which controller should execute.

---

## 3. RequestMappingHandlerAdapter

Purpose:

Actually invokes the controller method.

Responsibilities:

* Creates method arguments
* Injects RequestBody
* Injects PathVariable
* Injects RequestParam
* Calls the controller method

Without it:

Spring knows the method but cannot execute it.

---

## 4. HttpMessageConverters

Purpose:

Convert between Java Objects and HTTP.

Example

JSON → Java

```json
{
  "name":"Avatar"
}
```

↓

```java
Movie movie
```

Java → JSON

```java
Movie movie
```

↓

```json
{
  "name":"Avatar"
}
```

Common converter:

```
MappingJackson2HttpMessageConverter
```

Without it:

`@RequestBody`

and

`ResponseEntity<Movie>`

cannot work.

---

## 5. ConversionService

Purpose:

Converts request values into Java types.

Example:

```
/movies/10
```

↓

```java
Long id
```

or

```
?price=100
```

↓

```java
Integer
```

Without it:

Spring cannot convert request strings into Java objects.

---

## 6. Validator

Purpose:

Supports Bean Validation.

Example:

```java
@NotBlank

@Email

@Min
```

Used together with

```java
@Valid
```

Without it:

No validation occurs.

---

## 7. HandlerExceptionResolver

Purpose:

Handles exceptions thrown from controllers.

Example:

```java
@ControllerAdvice
```

↓

Converts exception into HTTP response.

Without it:

Exceptions become generic server errors.

---

## 8. ContentNegotiationManager

Purpose:

Determines response type.

Example:

```
Accept: application/json
```

↓

Return JSON.

Could also return:

* XML
* HTML

depending on configuration.

---

## 9. ViewResolver

Purpose:

Used mainly in traditional MVC applications.

Example

```
return "home";
```

↓

```
/WEB-INF/views/home.jsp
```

For REST APIs this is usually unused.

---

## 10. HandlerMethodArgumentResolvers

Purpose:

Inject controller parameters.

Examples:

```java
@PathVariable

@RequestBody

@RequestParam

@RequestHeader

Principal

Locale
```

Without these:

Controller parameters cannot be populated.

---

## 11. Static Resource Handling

Allows serving

```
CSS

JavaScript

Images

Fonts
```

without writing custom servlets.

---

## 12. Default MVC Configuration

Registers many sensible defaults such as:

* Default converters
* Default validators
* Default argument resolvers
* Default exception resolvers

---

# Summary of What `@EnableWebMvc` Gives

```
✔ DispatcherServlet support

✔ URL Mapping

✔ Controller invocation

✔ JSON conversion

✔ Validation

✔ Exception Handling

✔ Static resources

✔ Type conversion

✔ View Resolution

✔ Content Negotiation
```

---

# What is `WebMvcConfigurer`?

`WebMvcConfigurer` **does not create Spring MVC.**

It only lets us **customize** what `@EnableWebMvc` already created.

Think of it like this:

```
@EnableWebMvc

↓

Build the framework

↓

WebMvcConfigurer

↓

Customize the framework
```

---

# Common Customizations

## 1. extendMessageConverters()

Purpose:

Customize JSON conversion.

Example:

```java
ObjectMapper mapper = new ObjectMapper();
mapper.registerModule(new JavaTimeModule());
```

Why?

Jackson cannot serialize

```
LocalDate

LocalDateTime

Instant
```

without extra modules.

---

## 2. addCorsMappings()

Purpose:

Allow frontend applications to access backend.

Example

```
React

Angular

Vue
```

Without it:

Browser blocks requests.

---

## 3. addResourceHandlers()

Purpose:

Serve static files.

Example:

```
/images

/css

/js
```

---

## 4. addInterceptors()

Purpose:

Run code before or after controllers.

Typical use cases:

* Authentication
* Authorization
* Logging
* Timing
* Audit

---

## 5. addFormatters()

Purpose:

Custom type conversion.

Example:

```
"2026-08-06"

↓

LocalDate
```

---

## 6. configureMessageConverters()

Replace all default converters.

Use only when you want complete control.

Usually:

```
Avoid.
```

Instead use:

```
extendMessageConverters()
```

---

## 7. getValidator()

Provide a custom Validator implementation.

Useful when customizing validation behavior.

---

## 8. configurePathMatch()

Customize URL matching.

Example:

```
Trailing slash rules

Suffix matching

Case sensitivity
```

---

## 9. configureAsyncSupport()

Configure asynchronous request handling.

Useful for:

* Long-running APIs
* Streaming
* Deferred responses

---

# When Should We Override These?

| Method                  | Typical reason                                           |
| ----------------------- | -------------------------------------------------------- |
| extendMessageConverters | Register custom Jackson modules (e.g., `JavaTimeModule`) |
| addCorsMappings         | React/Angular frontend                                   |
| addInterceptors         | Authentication, logging                                  |
| addResourceHandlers     | Static resources                                         |
| addFormatters           | Custom date or enum conversion                           |
| getValidator            | Custom validation setup                                  |
| configurePathMatch      | URL matching customization                               |
| configureAsyncSupport   | Async APIs                                               |

---

# Relationship Between the Annotations

## `@Configuration`

Meaning:

```
This class contains Spring bean definitions.
```

Without it:

Spring ignores the class.

---

## `@ComponentScan`

Meaning:

```
Find my Controllers

Services

Repositories

@ControllerAdvice

Components
```

Without it:

No application beans are discovered automatically.

---

## `@EnableWebMvc`

Meaning:

```
Build the complete Spring MVC framework.
```

Registers all required MVC infrastructure.

---

## `WebMvcConfigurer`

Meaning:

```
I want to customize Spring MVC.
```

Nothing is created here.

Only customized.

---

# Complete Mental Model

```
Tomcat
    │
    ▼
DispatcherServlet
    │
    ▼
RequestMappingHandlerMapping
    │
    ▼
RequestMappingHandlerAdapter
    │
    ▼
ArgumentResolvers
    │
    ▼
Validation
    │
    ▼
Controller
    │
    ▼
Service
    │
    ▼
Return Java Object
    │
    ▼
HttpMessageConverter
    │
    ▼
JSON
    │
    ▼
Browser
```

If any exception occurs during the flow:

```
Exception
    │
    ▼
HandlerExceptionResolver
    │
    ▼
@ControllerAdvice
    │
    ▼
Error Response
```

---

# Key Takeaways

* `@Configuration` tells Spring **this class contains configuration**.
* `@ComponentScan` tells Spring **which application beans to discover**.
* `@EnableWebMvc` **creates the MVC infrastructure** required for a web application.
* `WebMvcConfigurer` **customizes** that infrastructure without replacing it.
* Most Spring Boot applications hide these details through auto-configuration. Building Spring MVC manually reveals the underlying architecture and makes debugging much easier because you know which component is responsible for each step of request processing.
