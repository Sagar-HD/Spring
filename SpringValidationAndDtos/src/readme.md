# Spring Boot DTO & Bean Validation Notes

## Overview

This project demonstrates **DTO-based request/response handling** and **Bean Validation** in Spring Boot **without using a database or JPA**.

The focus is to understand:

* Why DTOs are used.
* How Spring performs validation.
* Difference between `@Valid` and `@Validated`.
* Validation lifecycle inside Spring MVC.
* Nested object validation.
* Collection element validation.

---

# Why DTOs?

DTO stands for **Data Transfer Object**.

A DTO is an object whose only responsibility is to transfer data between two layers.

Examples:

```
HTTP Request
        ↓
Request DTO
        ↓
Service
        ↓
Response DTO
        ↓
HTTP Response
```

---

## Why not use Entity directly?

Although we are not using JPA yet, it's important to understand why Spring applications usually avoid exposing entities directly.

### 1. Separation of Concerns

A request from the client rarely matches the internal model of the application.

Example:

```
Client
--------
name
email
password
```

Internal model may contain

```
id
createdAt
updatedAt
role
status
passwordHash
```

The client should never send or receive these fields.

---

### 2. Security

Suppose an entity contains

```
passwordHash
role
salary
createdBy
```

Returning the entity directly exposes information that should remain internal.

A Response DTO returns only the required fields.

---

### 3. Different Requests Need Different Fields

Registration Request

```
name
email
password
```

Login Request

```
email
password
```

Profile Update

```
phone
address
```

Trying to use one entity for every request becomes difficult and error-prone.

---

### 4. Validation

Validation rules belong to incoming data.

Example

```java
@NotBlank
@Email
private String email;
```

These constraints describe what a client is allowed to send, making Request DTOs the natural place for validation.

---

### 5. Loose Coupling

Changing internal models should not force API changes.

The API contract remains stable because clients communicate through DTOs rather than domain objects.

---

# Request DTO vs Response DTO

## Request DTO

Represents incoming data.

Example

```
UserRequest
```

Contains validation annotations.

```
@NotBlank
@Email
@Pattern
@Size
```

---

## Response DTO

Represents outgoing data.

Never expose unnecessary information.

Example

```
UserResponse

id
name
email
```

Do **not** return passwords or other sensitive information.

---

# Validation Dependency

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

Spring Boot uses **Hibernate Validator** as the default implementation of the Jakarta Bean Validation specification.

---

# Validation Annotations Used

### String

* `@NotBlank`
* `@Email`
* `@Pattern`
* `@Size`

---

### Numbers

* `@NotNull`
* `@Min`
* `@Max`
* `@Positive`
* `@PositiveOrZero`
* `@Negative`
* `@NegativeOrZero`
* `@DecimalMin`
* `@DecimalMax`

---

### Collections

* `@NotEmpty`
* `@Size`
* Container Element Validation

Example

```java
private List<@NotBlank String> skills;
```

---

### Nested Objects

```java
@Valid
private AddressDto address;
```

---

# Validation Flow

```
HTTP Request
      │
      ▼
DispatcherServlet
      │
      ▼
RequestResponseBodyMethodProcessor
      │
      ▼
Jackson
(JSON → DTO)
      │
      ▼
@Valid ?
      │
      ▼
Validator.validate(dto)
      │
      ├──────── Validation Passed
      │               │
      │               ▼
      │      Controller Method Executes
      │
      └──────── Validation Failed
                      │
                      ▼
MethodArgumentNotValidException
```

---

# Who Creates the DTO?

Not Spring IoC.

Not Application Context.

Not Tomcat.

**Jackson** creates the DTO.

Conceptually

```java
UserRequest dto = objectMapper.readValue(json, UserRequest.class);
```

After creation, Spring performs validation.

---

# Is Request DTO a Spring Bean?

No.

It is a normal Java object (POJO).

It is **not** stored inside the Application Context.

It exists only during that request.

After the request completes, it becomes eligible for garbage collection.

---

# How Validation Works

Bean Validation validates **targets**, not arbitrary values.

There are two major targets.

---

## 1. Object Validation

```
validator.validate(object)
```

Example

```java
@Valid
@RequestBody UserRequest request
```

The validator inspects the object's fields using reflection.

```
UserRequest
      │
      ▼
Fields
      │
      ▼
@NotBlank
@Email
@Pattern
```

It reads the annotation metadata and the corresponding field values from the object instance.

---

## 2. Method Validation

```
ExecutableValidator.validateParameters(...)
```

Example

```java
@GetMapping("/{id}")
public User get(
    @Positive
    @PathVariable Long id
)
```

Here there is no DTO.

Instead, the validator validates:

* Method metadata
* Parameter annotations
* Runtime argument values

---

# Why @Valid?

`@Valid` tells Spring:

> Validate this object before invoking the controller method.

Without it

```
JSON

↓

Jackson

↓

Controller
```

With it

```
JSON

↓

Jackson

↓

Validator.validate(dto)

↓

Controller
```

---

# Why @Validated?

`@Valid` performs **object validation**.

Method parameters such as `@PathVariable` and `@RequestParam` are **not DTOs**.

Spring therefore performs **method validation**.

Example

```java
@Validated
@RestController
public class UserController {
}
```

Now Spring creates a proxy around the controller.

```
DispatcherServlet
        │
        ▼
Validation Proxy
        │
        ▼
ExecutableValidator.validateParameters(...)
        │
        ▼
Real Controller Method
```

Without `@Validated`, parameter constraints such as `@Positive` are ignored.

---

# @Valid vs @Validated

## @Valid

Standard Jakarta Bean Validation annotation.

Used for

* RequestBody
* Nested DTOs
* Object graph validation

Internally

```
validator.validate(object)
```

---

## @Validated

Spring annotation.

Used for

* Method parameters
* Path variables
* Request parameters
* Method return values

Internally

```
ExecutableValidator.validateParameters(...)
```

Enabled through Spring AOP proxies.

---

# Nested Validation

Without

```java
@NotNull
private AddressDto address;
```

Only the existence of the object is validated.

With

```java
@NotNull
@Valid
private AddressDto address;
```

Spring recursively validates every field inside `AddressDto`.

This is called **cascading validation**.

---

# Collection Element Validation

Instead of validating only the collection

```java
@NotEmpty
private List<String> skills;
```

Validate each element

```java
private List<
        @NotBlank
        String
> skills;
```

Now every element inside the collection is validated.

---

# Important Concepts Learned

* DTOs separate API contracts from internal models.
* Request DTOs and Response DTOs have different responsibilities.
* Jackson creates DTO instances.
* Request DTOs are **not** Spring beans.
* Bean Validation validates objects or method invocations.
* `@Valid` triggers object validation.
* `@Validated` enables method validation through Spring proxies.
* Nested DTO validation requires `@Valid`.
* Collections and their individual elements can both be validated.
* Validation occurs **before** the controller method executes.

---

# Key Takeaways

* Use **Request DTOs** to accept client input.
* Use **Response DTOs** to control API output.
* Never expose internal domain models directly.
* Validation belongs at the API boundary.
* `@Valid` validates objects.
* `@Validated` enables method-level validation.
* Spring MVC coordinates validation, while Hibernate Validator performs the actual constraint checks.
