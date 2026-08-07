# 🚀 Spring Boot Core – ApplicationRunner & External Configuration

This project demonstrates two fundamental Spring Boot concepts:

- Executing custom startup logic using `ApplicationRunner`.
- Reading external configuration from `application.properties` using `@Value`.

Unlike the previous project, which focused on the transition from Spring Framework to Spring Boot, this project explores how Spring Boot loads external configuration, injects property values into beans, performs dependency injection, and executes startup logic after the application is fully initialized.

---

# Concepts Covered

- `@SpringBootApplication`
- `@Component`
- Constructor Dependency Injection
- `@Value`
- `application.properties`
- `Environment`
- `ApplicationRunner`
- Spring Boot Startup Lifecycle

---

# Project Structure

```text
com.sagar
│
├── SpringBootApp
├── OrderService
├── PaymentService
└── application.properties
```

---

# SpringBootApp

```java
@SpringBootApplication
public class SpringBootApp {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootApp.class, args);
    }

}
```

The application starts using

```java
SpringApplication.run(...)
```

This is the entry point of every Spring Boot application.

During startup Spring Boot:

- Creates the Spring IoC Container (`ApplicationContext`)
- Prepares the application's `Environment`
- Loads configuration from `application.properties`
- Registers bean definitions through Component Scanning and Auto Configuration
- Creates and initializes beans
- Performs dependency injection
- Invokes lifecycle callbacks
- Executes all `ApplicationRunner` beans once the application is fully ready

---

# PaymentService

`PaymentService` demonstrates how Spring Boot injects external configuration into Spring beans.

```java
@Component
public class PaymentService
```

The bean receives configuration values in two different ways.

## Constructor Property Injection

```java
public PaymentService(
    @Value("${payment.name}") String paymentName
)
```

While creating the bean, Spring resolves the property from the application's `Environment` and passes it as a constructor argument.

This approach is useful when the value is required during object creation.

---

## Field Property Injection

```java
@Value("${payment.noOfRetries}")
private int noOfRetries;
```

After creating the object, Spring resolves the property from the `Environment` and injects it during the bean property population phase, before the bean becomes fully initialized.

---

# OrderService

```java
@Component
public class OrderService implements ApplicationRunner
```

This bean demonstrates three important Spring Boot concepts.

---

## Constructor Dependency Injection

```java
public OrderService(PaymentService paymentService)
```

Spring inspects the constructor, detects that it requires a `PaymentService`, resolves the matching bean from the BeanFactory, and passes it to the constructor while creating the object.

Because the class has only one constructor, no `@Autowired` annotation is required.

Constructor Injection is the recommended approach because required dependencies are available as soon as the object is created.

---

## Property Injection

```java
@Value("${deployer.name}")
private String deployerName;
```

Spring resolves the property from the `Environment` and injects it before the bean becomes ready.

---

## ApplicationRunner

```java
public void run(ApplicationArguments args)
```

This method executes automatically **once** after the entire Spring Boot application has finished starting.

By the time this method executes:

- All beans have been created
- Dependencies have been injected
- Property values have been injected
- `@PostConstruct` methods have already executed
- The `ApplicationContext` is completely initialized

Typical use cases include:

- Initializing data
- Running startup logic
- Performing validation
- Loading caches
- Printing startup information
- Scheduling initial tasks

---

# application.properties

Spring Boot automatically loads

```text
src/main/resources/application.properties
```

Example:

```properties
deployer.name=Sagar
payment.name=PhonePe
payment.noOfRetries=3
```

Unlike traditional Spring Framework applications, no `@PropertySource` annotation is required.

Spring Boot automatically discovers and loads `application.properties` during startup.

The loaded properties are stored inside Spring's `Environment`.

Whenever Spring encounters

```java
@Value("${property.name}")
```

it retrieves the corresponding value from the `Environment` and injects it into the bean.

---

# Spring Boot Startup Sequence

When the application starts, Spring Boot performs the following steps.

```text
SpringApplication.run()
        │
        ▼
Create ApplicationContext
        │
        ▼
Prepare Environment
        │
        ▼
Load application.properties
        │
        ▼
Register Bean Definitions
(Component Scan + AutoConfiguration)
        │
        ▼
BeanFactoryPostProcessors
        │
        ▼
Instantiate Beans
        │
        ▼
Constructor Dependency Injection
        │
        ▼
Property Population
(@Autowired, @Value, etc.)
        │
        ▼
Aware Interface Callbacks
        │
        ▼
BeanPostProcessor
(before initialization)
        │
        ▼
@PostConstruct
        │
        ▼
BeanPostProcessor
(after initialization)
        │
        ▼
ApplicationContext Ready
        │
        ▼
ApplicationRunner.run()
```

Notice that `ApplicationRunner` executes only after every bean has been completely initialized.

---

# Dependency Injection Used

This project demonstrates Constructor Injection.

```java
public OrderService(PaymentService paymentService)
```

Spring reads the constructor, identifies the required dependency, finds the corresponding bean definition inside the BeanFactory, creates the dependency if necessary, and passes the bean into the constructor.

Since there is only one constructor, Spring automatically performs constructor injection without requiring `@Autowired`.

---

# Property Injection Used

The project demonstrates two ways of injecting configuration values.

## Constructor Parameter

```java
@Value("${payment.name}")
```

Spring resolves the property from the `Environment` and supplies it while creating the object.

---

## Field Injection

```java
@Value("${payment.noOfRetries}")
```

Spring injects the property after object creation during the bean property population phase and before lifecycle callbacks such as `@PostConstruct`.

---

# Key Learnings

- Spring Boot automatically discovers and loads `application.properties`.
- External configuration is stored inside Spring's `Environment`.
- `@Value` resolves configuration values from the `Environment` and injects them into Spring beans.
- Constructor Injection is the preferred approach for injecting bean dependencies.
- Spring automatically resolves constructor dependencies from the BeanFactory.
- Property values are injected before the bean becomes fully initialized.
- `ApplicationRunner` executes exactly once after the `ApplicationContext` is completely ready.
- By the time `ApplicationRunner.run()` executes, all beans have been created, dependencies injected, property values resolved, and lifecycle callbacks such as `@PostConstruct` have already completed.
- Spring Boot manages the complete application startup lifecycle, allowing developers to focus on business logic rather than manual configuration.

---

# Conclusion

This project provides a practical understanding of how Spring Boot initializes an application from startup to execution. It demonstrates how external configuration is loaded into the `Environment`, how bean dependencies and property values are resolved and injected, and how startup logic can be executed safely using `ApplicationRunner` after the entire application context is fully initialized.