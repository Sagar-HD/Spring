# 🚀 Filters & Interceptors 
---

## 🧠 0. One-Look Understanding (Onion Model)

```text
Client
  ↓
[ FILTERS (Servlet / Tomcat) ]
  ↓
DispatcherServlet (Spring Entry Point)
  ↓
[ INTERCEPTORS (Spring / Handler Level) ]
  ↓
Controller (Handler Method)
  ↓
[ INTERCEPTORS (reverse) ]
  ↓
DispatcherServlet
  ↓
[ FILTERS (reverse) ]
  ↓
Client
```

👉 Outer = generic (Filters)
👉 Inner = Spring-specific (Interceptors)
👉 Each layer wraps the next (🧅 onion)

---

# 🔹 1. FILTERS (Servlet Level – Before Spring)

## 📌 Core Idea

* From **Jakarta Servlet API**
* Managed by **Tomcat**
* Runs **before Spring starts**

---

## ⚙️ Lifecycle

```text
init()      → once at startup
doFilter()  → every request
destroy()   → shutdown
```

---

## 🔁 Execution Visualization

```text
Request →
   Filter1 (before)
     Filter2 (before)
       ↓
       DispatcherServlet → Controller
       ↑
     Filter2 (after)
   Filter1 (after)
← Response
```

---

## 🔥 Registration (Important Difference)

### ✔ Spring Boot (Embedded Tomcat)

```java
@Component
public class MyFilter implements Filter {}
```

### ✔ Best (Control + Demo)

```java
@Bean
public FilterRegistrationBean<MyFilter> filter(){
    FilterRegistrationBean<MyFilter> reg = new FilterRegistrationBean<>();
    reg.setFilter(new MyFilter());
    reg.addUrlPatterns("/api/*");
    reg.setOrder(1);
    return reg;
}
```

---

### ✔ Spring MVC (External Tomcat)

* Register manually via:

    * `web.xml` OR
    * `ServletContext`

👉 Shows: Spring ≠ Container

---

## 🔑 Important Concepts

### ✔ Filter Chain

```text
F1 → F2 → Controller → F2 → F1
```

---

### ✔ Order

```java
reg.setOrder(1);
```

---

### ✔ Dispatcher Types

* REQUEST
* FORWARD
* INCLUDE
* ERROR
* ASYNC

---

### ✔ URL Mapping

* `/*` → global
* `/api/*` → selective

---

## ⚠️ Request/Response Reality (CRITICAL)

* Request body = **stream (read once)**
* Response body = **write once**

👉 If filter reads request → controller gets EMPTY

---

### ✔ Wrapper Solution (Rare Use)

* `HttpServletRequestWrapper`
* `HttpServletResponseWrapper`

👉 Used for:

* logging
* auditing

❌ Not for normal logic

---

## 🔥 OncePerRequestFilter (Spring Helper)

* Ensures **single execution per request**
* Avoids duplicate runs (forward/include)

---

## ⚡ Blocking Request

```java
if(invalid){
    response.getWriter().write("Blocked");
    return;
}
chain.doFilter(request, response);
```

---

## ⚡ Response Time (Demo Feature)

```java
long start = System.currentTimeMillis();

chain.doFilter(request, response);

long end = System.currentTimeMillis();
System.out.println("Time: " + (end - s
tart));
```

---

# 🔹 2. INTERCEPTORS (Spring Level – After Dispatcher)

## 📌 Core Idea

* Spring feature
* Runs **after DispatcherServlet**
* Works on **Handler (Controller method / static resource)**

👉 “HandlerInterceptor” because not just controllers

---

## 🔁 Full Flow (Accurate)

```text
Tomcat
 ↓
Filter Chain
 ↓
DispatcherServlet
 ↓
HandlerMapping → finds Controller + Method
 ↓
Interceptor preHandle()
 ↓
HandlerAdapter → calls controller method
 ↓
Controller executes
 ↓
Interceptor postHandle()
 ↓
View / JSON (Jackson serialization happens here)
 ↓
Interceptor afterCompletion()
 ↓
DispatcherServlet
 ↓
Filter Chain (response phase)
 ↓
Client
```

---

## ⚙️ 3 Methods

### ✔ preHandle()

```java
boolean preHandle(...)
```

* Before controller
* `true` → continue
* `false` → stop

---

### ✔ postHandle()

```java
void postHandle(...)
```

* After controller
* Before view rendering

👉 Used in MVC (views), rarely in REST

---

### ✔ afterCompletion()

```java
void afterCompletion(...)
```

* After full request
* Has exception info

👉 Best for:

* logging
* cleanup

---

## 🔥 postHandle vs afterCompletion

| Feature          | postHandle       | afterCompletion    |
| ---------------- | ---------------- | ------------------ |
| When             | After controller | After full request |
| View rendered    | ❌                | ✅                  |
| Exception access | ❌                | ✅                  |
| REST usage       | Rare             | Common             |

---

## ⚙️ Registration

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MyInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/login")
                .order(1);
    }
}
```

---

## 🔑 Execution Order

```text
preHandle      → 1 → 2
postHandle     → 2 → 1
afterCompletion→ 2 → 1
```

---

## 🔑 Handler Object

* Represents controller method

```java
handler.toString()
```

👉 Can log:

* method
* class
* mapping

---

## 🔥 Logging (Your Demo Must-Have)

Log:

* HTTP method
* URL
* IP address
* Controller method

---

## 🔐 Auth Data Passing (Important)

### ✔ Request Attribute (like Node but clean)

```java
request.setAttribute("userId", id);
```

---

### ✔ Best Practice (Spring Security)

```java
SecurityContextHolder.getContext().setAuthentication(auth);
```

---

### ✔ Controller Access

```java
@AuthenticationPrincipal User user
```

---

## 🔥 Real Usage Difference

| Use Case                  | Filter | Interceptor |
| ------------------------- | ------ | ----------- |
| CORS                      | ✅      | ❌           |
| JWT Auth (real)           | ✅      | ❌           |
| Logging (global)          | ✅      | ✅           |
| Controller-specific logic | ❌      | ✅           |
| Role-based method control | ❌      | ✅           |

---

## 🧠 FINAL DEMO FLOW (Say This Clearly)

```text
Client
 → Filter (logging / timing / auth)
   → DispatcherServlet
     → Interceptor preHandle (log, auth check)
       → Controller
     → Interceptor postHandle
     → Response (Jackson)
     → Interceptor afterCompletion
 → Filter (response phase)
Client
```

---

## 🏁 FINAL TAKEAWAYS

* Filter = **Servlet level (before Spring)**
* Interceptor = **Spring level (around controller)**
* Filters = **generic concerns**
* Interceptors = **handler-specific logic**
* Spring Security = **built on filters**

---

🔥 If this flow is clear in your head → you understand Spring request lifecycle like a pro
