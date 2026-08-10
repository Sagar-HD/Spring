# 🚀 Spring Web MVC Architecture: External Tomcat vs Embedded Tomcat

> **Goal:** Understand what really happens from JVM startup until a browser receives a response.

---

# 🌍 Big Picture

At the highest level, **both architectures are almost identical once Tomcat is running.**

The only real difference is:

| External Tomcat                | Embedded Tomcat                   |
| ------------------------------ | --------------------------------- |
| Tomcat starts your application | Your application starts Tomcat    |
| WAR deployment                 | Executable JAR                    |
| Tomcat owns JVM startup        | Your application owns JVM startup |

Everything after Tomcat is started is almost the same.

---

# 1️⃣ External Tomcat Architecture

```
          User Starts Tomcat
                 │
                 ▼
         JVM Starts Tomcat
                 │
                 ▼
        Tomcat main() executes
                 │
                 ▼
     Opens HTTP Port (8080/9090)
                 │
                 ▼
      Detects deployed WAR file
                 │
                 ▼
      Creates ServletContext
                 │
                 ▼
      Starts Spring Framework
                 │
                 ▼
Creates Spring WebApplicationContext
                 │
                 ▼
Creates DispatcherServlet
                 │
                 ▼
Registers DispatcherServlet
inside ServletContext
                 │
                 ▼
 Application Ready
```

---

## WAR Structure

Tomcat understands this structure directly.

```
myapp.war

│
├── index.jsp
├── static/
│
└── WEB-INF
      │
      ├── web.xml
      │
      ├── classes
      │      ├── Controller.class
      │      ├── Service.class
      │      └── ...
      │
      └── lib
             ├── spring-web.jar
             ├── jackson.jar
             ├── mysql.jar
             └── ...
```

Tomcat loads

```
WEB-INF/classes
WEB-INF/lib
```

using its own classloader.

---

# 2️⃣ Embedded Tomcat Architecture (Spring Boot)

This is what we use today.

```
java -jar app.jar

        │
        ▼
      JVM Starts
        │
        ▼
Main.main()
        │
        ▼
SpringApplication.run()
        │
        ▼
Creates ApplicationContext
        │
        ▼
Creates all Spring Beans
        │
        ▼
Creates DispatcherServlet
        │
        ▼
Creates Embedded Tomcat
        │
        ▼
Registers DispatcherServlet
        │
        ▼
Tomcat.start()
        │
        ▼
Application Ready
```

Notice something interesting.

Spring creates

```
DispatcherServlet
```

**before Tomcat starts accepting requests.**

Then Spring says

> "Tomcat, whenever a request comes, execute this servlet."

Tomcat simply stores it inside its `ServletContext`.

---

# Spring Boot JAR Structure

Instead of

```
WEB-INF
```

Spring Boot packages everything inside

```
BOOT-INF
```

```
app.jar

│
├── META-INF
│
├── BOOT-INF
│      │
│      ├── classes
│      │      ├── Controller.class
│      │      ├── Service.class
│      │      └── ...
│      │
│      └── lib
│             ├── spring-web.jar
│             ├── tomcat.jar
│             ├── jackson.jar
│             └── ...
│
└── Spring Boot Loader
```

Tomcat never directly reads `BOOT-INF`.

Spring Boot's launcher loads these classes and JARs first, builds the class loader, and only then starts Tomcat.

---

# ⭐ The Biggest Clarification

Many people think

```
Embedded Tomcat

↓

doesn't use a web application
```

❌ Wrong.

It absolutely does.

Internally Tomcat still creates

```
ServletContext

↓

Servlet Registration

↓

DispatcherServlet

↓

Servlet Mapping

↓

Filters

↓

Listeners
```

The only difference is

Instead of reading

```
web.xml
```

Tomcat receives all this information programmatically from Spring Boot.

---

# Startup Comparison

## External

```
Tomcat main()

↓

Create ServletContext

↓

Load WAR

↓

Load Spring

↓

Create DispatcherServlet

↓

Ready
```

---

## Embedded

```
Main.main()

↓

SpringApplication.run()

↓

Create ApplicationContext

↓

Create DispatcherServlet

↓

Create Tomcat

↓

Tomcat.start()

↓

Ready
```

---

# Request Flow (Both are IDENTICAL)

```
Browser

↓

Operating System

↓

Port 8080

↓

Tomcat Acceptor Thread

↓

Tomcat Worker Thread

↓

DispatcherServlet

↓

HandlerMapping

↓

Controller

↓

Service

↓

Repository

↓

Database

↓

Repository

↓

Service

↓

Controller

↓

DispatcherServlet

↓

Tomcat

↓

Browser
```

This part is **100% identical** in both deployments.

---

# Thread Architecture

```
                    JVM

                      │

        ┌─────────────┴─────────────┐

        │                           │

 Main Thread                 JVM Threads

 (startup only)          GC, JIT, Reference...

        │

        ▼

Creates ApplicationContext

Creates Beans

Creates DispatcherServlet

Creates Tomcat

Calls tomcat.start()

        │

        ▼

Main Thread Finishes

        │

──────────────────────────────────────────────

Tomcat Threads Continue Running

        │

        ├── Acceptor Thread
        │       │
        │       └── waits for connections
        │
        ├── Worker Thread 1
        ├── Worker Thread 2
        ├── Worker Thread 3
        └── Worker Thread N

──────────────────────────────────────────────

Optional Spring Threads

        ├── @Scheduled
        ├── @Async
        ├── Kafka Consumers
        ├── RabbitMQ
        ├── Event Executors
        └── Hikari Housekeeping
```

Notice:

> **Spring ApplicationContext is NOT a thread.**

It is only a container holding objects.

---

# How DispatcherServlet Uses Spring

During startup

```
ApplicationContext

↓

creates UserController

↓

creates UserService

↓

creates UserRepository
```

DispatcherServlet receives a reference to this ApplicationContext.

```
DispatcherServlet

↓

ApplicationContext
```

Now every request

```
GET /users
```

becomes

```
DispatcherServlet

↓

HandlerMapping

↓

ApplicationContext

↓

Give me UserController bean

↓

Controller executes

↓

Response
```

DispatcherServlet never creates controllers.

It only asks Spring

> "Give me the already-created bean."

---

# Does Tomcat Need WEB-INF in Embedded Mode?

### Physically?

❌ No.

There is no `WEB-INF` folder inside the executable JAR.

---

### Logically?

✅ Yes.

Tomcat still needs

* ServletContext
* DispatcherServlet
* Servlet Mapping
* Filters
* Listeners

Spring Boot simply builds this web application **in memory** instead of reading it from a WAR file.

Think of it like this:

### External Tomcat

```
Tomcat

↓

Reads WAR

↓

Builds Web Application
```

### Embedded Tomcat

```
Spring Boot

↓

Builds Web Application

↓

Hands it to Tomcat
```

Tomcat itself doesn't care whether the web application came from a WAR on disk or was assembled in memory.

---

# 🎯 Final Mental Model

Think of Tomcat as a **movie projector**.

The projector doesn't care whether the movie comes from:

* a DVD 💿 (WAR)
* a USB drive 💾 (BOOT-INF)
* memory 📦 (Spring Boot)

It only needs a movie to project.

Similarly, Tomcat doesn't care whether your application comes from `WEB-INF` on disk or is assembled in memory by Spring Boot. By the time `tomcat.start()` finishes, Tomcat has the same things it always needs: a `ServletContext`, a registered `DispatcherServlet`, URL mappings, filters, listeners, and a class loader. From that point onward, request processing is essentially identical in both external and embedded deployments.
