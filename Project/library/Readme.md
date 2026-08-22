Absolutely. Here’s a **clean, GitHub-ready README**—less bloated, professional, and focused on what you actually implemented.

````markdown
# 📚 Library Management System

A secure RESTful Library Management API built with **Spring Boot** to practice and integrate core Spring ecosystem concepts.

The project goes beyond basic CRUD by implementing authentication, authorization, JWT, JPA/Hibernate, transactions, validation, business rules, AOP, and Spring MVC interceptors.

---

## 🚀 Features

- JWT-based authentication
- Role-based authorization
- User and librarian management
- Book management
- Book borrowing workflow
- Librarian approval workflow
- Book return workflow
- Borrowing history
- Filtering and sorting of borrow records
- Bean Validation
- Global exception handling
- Transaction management
- Hibernate dirty checking
- Spring Data JPA derived queries
- AOP-based logging
- Custom AOP annotation for execution-time measurement
- HTTP Request ID interceptor
- MySQL persistence

---

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot**
- **Spring MVC**
- **Spring Security**
- **JWT**
- **Spring Data JPA**
- **Hibernate**
- **MySQL**
- **Bean Validation**
- **Spring AOP**
- **Maven**
- **Postman**

---

## 🏗️ Architecture

```text
Client
  │
  ▼
Tomcat
  │
  ▼
JWT Filter
  │
  ▼
Spring Security
  │
  ▼
Request ID Interceptor
  │
  ▼
DispatcherServlet
  │
  ▼
Controller
  │
  ▼
AOP Proxy
  │
  ▼
Service
  │
  ▼
@Transactional
  │
  ▼
Repository
  │
  ▼
Spring Data JPA
  │
  ▼
Hibernate
  │
  ▼
JDBC
  │
  ▼
MySQL
````

---

# 🔐 Authentication & Authorization

The application uses **JWT-based stateless authentication**.

### Login flow

```text
Login Request
     ↓
AuthenticationManager
     ↓
DaoAuthenticationProvider
     ↓
UserDetailsService
     ↓
PasswordEncoder
     ↓
Authentication
     ↓
JWT Generation
     ↓
JWT Response
```

For authenticated requests:

```text
Authorization Header
        ↓
JwtFilter
        ↓
Extract JWT
        ↓
Validate JWT
        ↓
Load UserDetails
        ↓
Create Authentication
        ↓
SecurityContextHolder
        ↓
Controller
```

Passwords are hashed using BCrypt.

### Roles

* `USER`
* `LIBRARIAN`
* `ADMIN`

Method-level authorization is implemented using:

```java
@PreAuthorize(...)
```

---

# 📡 API Endpoints

## Authentication

| Method | Endpoint         | Description           |
| ------ | ---------------- | --------------------- |
| POST   | `/auth/register` | Register a user       |
| POST   | `/auth/login`    | Login and receive JWT |

### Register

```http
POST /auth/register
```

```json
{
  "name": "sagar",
  "email": "sagar@gmail.com",
  "password": "password123"
}
```

### Login

```http
POST /auth/login
```

```json
{
  "name": "sagar",
  "password": "password123"
}
```

Response:

```json
{
  "token": "<JWT>"
}
```

---

## 👤 User APIs

| Method | Endpoint           | Access            |
| ------ | ------------------ | ----------------- |
| GET    | `/users/me`        | Authenticated     |
| POST   | `/users`           | ADMIN / LIBRARIAN |
| POST   | `/users/librarian` | ADMIN             |

### Current user

```http
GET /users/me
Authorization: Bearer <JWT>
```

### Get all users

```http
POST /users
Authorization: Bearer <JWT>
```

### Add librarian

```http
POST /users/librarian
Authorization: Bearer <ADMIN_JWT>
```

---

# 📖 Book APIs

| Method | Endpoint      | Access            |
| ------ | ------------- | ----------------- |
| POST   | `/books`      | ADMIN / LIBRARIAN |
| GET    | `/books/{id}` | Authenticated     |
| GET    | `/books`      | Authenticated     |
| PUT    | `/books/{id}` | ADMIN / LIBRARIAN |
| DELETE | `/books/{id}` | ADMIN / LIBRARIAN |

### Add book

```http
POST /books
```

```json
{
  "name": "Clean Code",
  "authorName": "Robert C. Martin",
  "noOfCopies": 5
}
```

### Search books

Optional query parameters:

```text
title
authorName
```

Examples:

```http
GET /books
GET /books?title=clean
GET /books?authorName=martin
GET /books?title=clean&authorName=martin
```

---

# 📚 Borrow APIs

| Method | Endpoint                     | Description                  |
| ------ | ---------------------------- | ---------------------------- |
| POST   | `/borrow/{bookId}`           | Request a book               |
| POST   | `/borrow/{borrowId}/approve` | Approve request              |
| POST   | `/borrow/{borrowId}/return`  | Return book                  |
| GET    | `/borrow/my`                 | Get user's borrowing history |
| GET    | `/borrow`                    | Get all borrowing records    |

### Request a book

```http
POST /borrow/{bookId}
Authorization: Bearer <JWT>
```

The authenticated user's ID is obtained from:

```java
@AuthenticationPrincipal CustomUserDetails
```

The client does not provide the user ID.

### Approve borrowing

```http
POST /borrow/{borrowId}/approve
Authorization: Bearer <LIBRARIAN_JWT>
```

Changes:

```text
REQUESTED → BORROWED
```

### Return book

```http
POST /borrow/{borrowId}/return
Authorization: Bearer <JWT>
```

Changes:

```text
BORROWED → RETURNED
```

---

## 🔎 Borrow Record Filtering

Borrow records support optional filtering and sorting.

Query parameters:

```text
status
sortByDueDate
```

Examples:

```http
GET /borrow
GET /borrow?status=BORROWED
GET /borrow?sortByDueDate=true
GET /borrow?status=REQUESTED&sortByDueDate=true
```

For the authenticated user:

```http
GET /borrow/my
GET /borrow/my?status=BORROWED
GET /borrow/my?sortByDueDate=true
GET /borrow/my?status=RETURNED&sortByDueDate=true
```

---

# 🛡️ Business Rules

The project implements business rules beyond simple CRUD.

### Borrowing

* Cannot borrow an unavailable book.
* Cannot have multiple active requests for the same book.
* A request must be approved before becoming `BORROWED`.
* A book's available copies cannot become negative.

### Returning

* Only the user who borrowed the book can return it.
* Only `BORROWED` records can be returned.
* Returning a book increases its available copies.

### Book management

* Available copies cannot exceed total copies.
* Total copies cannot be reduced below the number of currently borrowed copies.
* A book cannot be deleted while copies are currently borrowed.

---

# 💾 Transactions

Transactions are used for operations where multiple state changes must remain consistent.

### Borrow request

```text
BEGIN
  ↓
Find User
  ↓
Find Book
  ↓
Check Availability
  ↓
Check Existing Borrow
  ↓
Create BorrowRecord
  ↓
COMMIT
```

### Approve request

```text
BEGIN
  ↓
Find BorrowRecord
  ↓
Validate REQUESTED status
  ↓
Check Book Availability
  ↓
REQUESTED → BORROWED
  ↓
Update Available Copies
  ↓
COMMIT
```

### Return book

```text
BEGIN
  ↓
Find BorrowRecord
  ↓
Validate Ownership
  ↓
Validate BORROWED status
  ↓
BORROWED → RETURNED
  ↓
Increase Available Copies
  ↓
COMMIT
```

The project also demonstrates Hibernate dirty checking by modifying managed entities inside transactions without explicitly saving every change.

---

# 🧩 JPA / Hibernate

The project uses:

* `@Entity`
* `@ManyToMany`
* `@OneToMany`
* `@ManyToOne`
* Persistence Context
* Managed entities
* Dirty checking
* Transactions
* Lazy loading
* Spring Data JPA repositories
* Derived query methods

Example repository queries:

```java
findByNameContainingIgnoreCase(...)
findByAuthorNameContainingIgnoreCase(...)
existsByBorrowedByIdAndBorrowedBookIdAndBorrowStatusIn(...)
getAllByBorrowedById(...)
getAllByBorrowStatus(...)
```

Hibernate SQL logging was enabled during development to observe the generated SQL.

---

# ⚠️ Exception Handling

Centralized exception handling is implemented using:

```java
@RestControllerAdvice
```

Custom exceptions include:

* `BookNotFoundException`
* `BookNotAvailableException`
* `AlreadyBorrowedOrInRequestedException`
* `BookLoanNotClearedBeforeDeleteException`
* `MaliciousAccessException`
* `BusinessException`

Validation failures are handled using:

```text
MethodArgumentNotValidException
        ↓
GlobalExceptionHandler
        ↓
400 Bad Request
```

---

# ✅ Validation

Bean Validation is applied to request DTOs using annotations such as:

```java
@NotBlank
@NotNull
@Email
@Size
```

The validation flow is:

```text
JSON Request
     ↓
Jackson
     ↓
DTO
     ↓
Bean Validation
     ↓
GlobalExceptionHandler
     ↓
Error Response
```

---

# 🧠 Spring AOP

A `LoggingAspect` is used for service-layer cross-cutting concerns.

Implemented advice types:

```text
@Before
@After
@AfterReturning
@AfterThrowing
@Around
```

Example:

```text
[Before] execution(BorrowRecordService.borrowBook(..))

[After returning] execution(BorrowRecordService.borrowBook(..))
```

Exception execution:

```text
[Before]
[After exception]
[After]
```

---

# ⏱️ Custom AOP Annotation

A custom annotation is used to measure execution time:

```java
@MeasureTime(threshold = 100)
```

The aspect measures the complete execution of the annotated method.

Example output:

```text
Time taken exceeded warning: 176ms
```

This demonstrates annotation-based pointcuts:

```text
@MeasureTime
     ↓
Spring AOP
     ↓
@Around
     ↓
ProceedingJoinPoint
     ↓
Target Method
```

---

# 🔍 Request ID Interceptor

A Spring MVC `HandlerInterceptor` generates a unique request ID.

Example:

```text
X-Request-ID:
7e5a5b4e-5af7-4f3d-8713-314a1e4368cd
```

Flow:

```text
HTTP Request
     ↓
RequestIdInterceptor
     ↓
Generate UUID
     ↓
Controller
     ↓
HTTP Response
     ↓
X-Request-ID
```

This provides a simple mechanism for identifying individual HTTP requests during debugging.

---

# 📁 Project Structure

```text
com.sagar.library
│
├── annotation
│   └── MeasureTime.java
│
├── aspect
│   └── LoggingAspect.java
│
├── config
│   ├── SecurityConfig.java
│   └── WebConfig.java
│
├── constant
│   └── BorrowStatus.java
│
├── controller
│   ├── AuthController.java
│   ├── BookController.java
│   ├── BorrowController.java
│   ├── RoleController.java
│   └── UserController.java
│
├── dto
│   ├── BookDto.java
│   ├── ErrorResponseDto.java
│   ├── UserLoginRequestDto.java
│   ├── UserLoginResponseDto.java
│   ├── UserRegistrationRequestDto.java
│   ├── UserResponseDto.java
│   └── ValidationErrorDto.java
│
├── entity
│   ├── Book.java
│   ├── BorrowRecord.java
│   ├── Role.java
│   └── User.java
│
├── exception
│   ├── GlobalExceptionHandler.java
│   ├── BusinessException.java
│   ├── BookNotFoundException.java
│   ├── BookNotAvailableException.java
│   ├── AlreadyBorrowedOrInRequestedException.java
│   ├── BookLoanNotClearedBeforeDeleteException.java
│   ├── MaliciousAccessException.java
│   └── UserNotFoundException.java
│
├── filter
│   └── JwtFilter.java
│
├── interceptor
│   └── RequestIdInterceptor.java
│
├── repository
│   ├── BookRepository.java
│   ├── BorrowRecordRepository.java
│   ├── RoleRepository.java
│   └── UserRepository.java
│
├── security
│   ├── CustomUserDetails.java
│   └── CustomUserDetailsService.java
│
├── service
│   ├── AuthService.java
│   ├── BookService.java
│   ├── BorrowRecordService.java
│   └── UserService.java
│
└── util
    └── JwtUtil.java
```

---

# ⚙️ Configuration

Example `application.properties`:

```properties
spring.application.name=library

spring.datasource.url=jdbc:mysql://localhost:3306/library
spring.datasource.username=root
spring.datasource.password=<YOUR_PASSWORD>

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

> Never commit real database passwords or JWT secrets to GitHub.

---

# ▶️ Running the Project

### Prerequisites

* Java 17+
* Maven
* MySQL

Create the database:

```sql
CREATE DATABASE library;
```

Configure your database credentials in:

```text
src/main/resources/application.properties
```

Run:

```bash
./mvnw spring-boot:run
```

Windows:

```bash
mvnw.cmd spring-boot:run
```

Application:

```text
http://localhost:8080
```

---

# 🧪 Testing

API testing was performed using Postman.

The tested flows include:

* User registration
* Login
* JWT authentication
* User profile
* User listing
* Librarian creation
* Book creation
* Book search
* Book update
* Book deletion
* Borrow request
* Borrow approval
* Book return
* Personal borrowing history
* Borrow record filtering
* Borrow record sorting
* Authorization failures
* Business rule violations
* Validation failures

---

# 📈 Learning Objectives

This project was built to move beyond a basic CRUD application and understand how different parts of the Spring ecosystem work together.

### Concepts practiced

```text
Spring Core
    ↓
Dependency Injection
    ↓
Spring MVC
    ↓
REST APIs
    ↓
DTOs
    ↓
Validation
    ↓
Exception Handling
    ↓
Spring Data JPA
    ↓
Hibernate
    ↓
Transactions
    ↓
Spring Security
    ↓
JWT
    ↓
AOP
    ↓
Interceptors
    ↓
MySQL
```

The main goal was not just to create CRUD endpoints, but to understand:

* Request processing
* Authentication
* Authorization
* Persistence context
* Entity state
* Dirty checking
* Transaction boundaries
* Business rules
* Cross-cutting concerns
* HTTP request interception
* Database interaction through Hibernate

---

# 🔮 Future Improvements

Possible future additions:

* Unit testing
* Integration testing
* Swagger / OpenAPI
* Redis caching
* Async processing
* Scheduled jobs
* Docker
* CI/CD
* Notifications
* Book reservation
* Fine calculation

Distributed-system technologies such as **microservices and Kafka** are intentionally outside the scope of this version.

---

## 📌 Project Status

**Completed — V1**

The project successfully integrates authentication, authorization, REST APIs, JPA/Hibernate, transactions, validation, exception handling, AOP, and Spring MVC interceptors into a single backend application.

```

