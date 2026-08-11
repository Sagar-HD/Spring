# 🚨 Exception Handling in Spring Boot

> **Goal:** Build applications that fail gracefully, return meaningful responses, and keep the code clean, maintainable, and production-ready.

---


# 🎯 Why Exception Handling?

Every application encounters unexpected situations.

Examples:

* User doesn't exist
* Invalid input
* Database unavailable
* File missing
* Payment failed

Instead of crashing the application or exposing internal details, exception handling helps us:

* ✅ Handle errors gracefully
* ✅ Keep business logic clean
* ✅ Return meaningful responses
* ✅ Maintain consistent API responses
* ✅ Log unexpected failures
* ✅ Improve maintainability

---

# ☕ Core Java Foundation

The golden rule of exception handling is:

> **Recover where possible. Otherwise, propagate the exception to a higher layer that is responsible for handling it.**

There are only four reasons to catch an exception:

## 1️⃣ Recover

```text
Read File

↓

File Not Found

↓

Ask user for another file

↓

Continue execution
```

---

## 2️⃣ Translate

Convert a low-level exception into a more meaningful exception.

```text
SQLException

↓

DataAccessException
```

---

## 3️⃣ Clean Up

Release resources before propagating.

Examples:

* Close files
* Close database connections
* Release locks

---

## 4️⃣ Convert into a Response

Typical in web applications.

```text
Exception

↓

HTTP Response
```

---

# 🌱 Exception Flow in Spring Boot

Suppose a request arrives.

```text
Client
   │
Tomcat
   │
DispatcherServlet
   │
Controller
   │
Service
   │
Repository
```

Repository throws:

```text
UserNotFoundException
```

Propagation:

```text
Repository
   ↑
Service
   ↑
Controller
   ↑
DispatcherServlet
```

DispatcherServlet now asks:

> "Can anyone handle this exception?"

---

# 🏗 Responsibilities of Each Layer

## Repository

Responsible for:

* Database operations

Cannot recover?

⬆️ Propagate

---

## Service

Responsible for:

* Business logic

Cannot recover?

⬆️ Propagate

---

## Controller

Responsible for:

* HTTP request mapping
* Request parsing
* Basic validation
* Calling service
* Returning result

The controller should **not** contain large try-catch blocks for every endpoint.

---

## DispatcherServlet

Acts as the **central coordinator** of Spring MVC.

Responsibilities:

* Dispatch request
* Invoke controller
* Coordinate exception handling
* Build HTTP response

---

# 🌍 Global Exception Handling

Instead of writing try-catch inside every controller...

❌ Bad

```text
UserController
    try-catch

OrderController
    try-catch

PaymentController
    try-catch
```

Use one global handler.

✅ Good

```text
UserController

OrderController

PaymentController

        │
        ▼
@RestControllerAdvice
```

This keeps controllers clean and follows the **Single Responsibility Principle**.

---

# ⚖️ Local vs Global Exception Handling

## Local Exception Handling

Use only when you can recover.

Example:

```text
Payment timeout

↓

Retry payment

↓

Continue
```

---

## Global Exception Handling

Used when recovery is not possible.

Responsibilities:

* Convert exception to HTTP response
* Decide status code
* Return Error DTO
* Log unexpected errors

---

# 🧩 Custom Business Exceptions

Instead of exposing framework exceptions, create business-specific exceptions.

Examples:

```text
UserNotFoundException

DuplicateEmailException

OrderNotFoundException

PaymentFailedException

InsufficientBalanceException
```

Don't create dozens of unnecessary exception classes.

❌

```text
UserNotFoundForLoginException

UserNotFoundForOrderException

UserNotFoundForProfileException
```

✅

```text
UserNotFoundException
```

Use the exception message to provide specific details.

Example:

```text
User with id 101 not found

Customer abc@gmail.com not found

Admin account not found
```

---

# 📦 Error Response DTO

Never return random JSON structures.

Instead define one standard response.

Example:

```text
status
message
timestamp
path
```

Benefits:

* Consistent API
* Easy frontend integration
* Professional design
* Easy future extension

---

# ✅ Validation Exception Handling

Validation is different.

One request may contain multiple validation failures.

Example:

```text
Name is empty

Email invalid

Age below minimum
```

Spring throws:

```text
MethodArgumentNotValidException
```

Inside this exception are all field validation errors.

Instead of returning only one message,

return

```text
Field: name
Message: Name is required

Field: email
Message: Invalid email

Field: age
Message: Must be at least 18
```

Many projects use a separate Validation Error DTO for this purpose.

---

# 🔄 Exception Translation

Spring doesn't expose low-level database exceptions directly.

Without Spring:

```text
JDBC Driver

↓

SQLException
```

With Spring:

```text
JDBC Driver

↓

SQLException

↓

JdbcTemplate

↓

SQLExceptionTranslator

↓

DataAccessException
```

This is called **Exception Translation**.

Purpose:

* Hide vendor-specific exceptions
* Provide consistent Spring exceptions
* Reduce coupling

---

# 📝 Logging

Returning an error response is **not enough**.

Unexpected exceptions should also be logged.

Typical flow:

```text
Exception

↓

Global Handler

↓

Log

↓

Return JSON Response
```

Business exceptions:

```text
User not found

Wrong password

Coupon expired
```

Usually log at INFO/WARN or according to project requirements.

Unexpected exceptions:

```text
NullPointerException

Database unavailable

Unexpected RuntimeException
```

Log as ERROR.

**Never use**

```text
printStackTrace()
```

in production.

Use a logging framework instead.

---

# 🌍 Spring vs Tomcat

## Your Global Handler

Handles:

* Business exceptions
* Application-specific exceptions
* Validation exceptions

Returns professional JSON responses.

---

## Spring Default Handlers

Handle many framework-related exceptions automatically.

Examples:

* Validation failures
* Missing request parameters
* Wrong HTTP method
* Type conversion errors

---

## Tomcat

Tomcat is the **final safety net**.

If an exception escapes Spring completely,

Tomcat ensures that the client still receives an HTTP error response.

Without Spring handling it, Tomcat's default response is typically an HTML error page (unless customized).

Tomcat's responsibility is:

> Never leave the client waiting forever.

---

# 🔁 Complete Request Flow

```text
Client
   │
Tomcat
   │
DispatcherServlet
   │
Controller
   │
Service
   │
Repository
   │
Exception
   ▲
Service
   ▲
Controller
   ▲
DispatcherServlet
   │
────────────────────────────────────
Your @RestControllerAdvice
────────────────────────────────────
        │
Handled?
│
├── Yes
│      │
│      ▼
│   Error DTO
│      │
│      ▼
│  HTTP Response
│
└── No
        │
        ▼
Spring Default Exception Resolvers
        │
Handled?
│
├── Yes
│      │
│      ▼
│  HTTP Response
│
└── No
        │
        ▼
Tomcat Error Handling
        │
        ▼
Default Error Response
        │
        ▼
Client
```

---

# ✅ Best Practices

* ✅ Recover only when recovery is possible.
* ✅ Otherwise propagate the exception.
* ✅ Keep controllers clean.
* ✅ Use `@RestControllerAdvice`.
* ✅ Create business-specific exceptions.
* ✅ Return consistent Error DTOs.
* ✅ Use a dedicated Validation Error DTO (or equivalent structure) for validation failures.
* ✅ Log unexpected exceptions.
* ✅ Don't expose stack traces to clients.
* ✅ Let each layer focus on its own responsibility.

---

# 🧠 Final Mental Model

Think of exception handling as a chain of responsibility.

```text
Problem occurs

↓

Can I recover?

├── Yes
│      Recover
│
└── No
        │
        ▼
Can I translate it?

├── Yes
│      Translate
│
└── No
        │
        ▼
Can I convert it into a response?

├── Yes
│      Return Response
│
└── No
        │
        ▼
Propagate to the next responsible layer
```

Every layer follows the same principle:

> **Handle only what you're responsible for. Otherwise, pass it to the next responsible layer.**

This pattern is used not only in Spring Boot, but throughout enterprise Java—including JDBC, Hibernate, JPA, Spring Security, transactions, messaging, and distributed systems.
