````markdown
# 🚀 Spring Core → Spring Boot (Core)

Spring Boot **does not replace Spring Framework**. It builds on top of Spring Framework and automates the repetitive configuration required in almost every Spring application.

Everything you learned about **IoC, Dependency Injection, Bean Lifecycle, BeanFactory, ApplicationContext, AOP, BeanFactoryPostProcessor, and BeanPostProcessor** still applies. Spring Boot simply configures and starts them automatically, allowing developers to focus on business logic instead of boilerplate setup.

---

# Overall Transition

| Spring Core | Spring Boot |
|--------------|-------------|
| Create `ApplicationContext` manually | `SpringApplication.run()` creates it |
| Register configuration manually | Automatically registers the main configuration |
| Configure component scanning | Automatic scanning from the main package |
| Add Spring modules individually | Add one starter dependency |
| Specify every dependency version | Parent POM manages versions |
| Load property files using `@PropertySource` | Automatically loads `application.properties` |
| Configure common infrastructure yourself | Auto Configuration creates it |
| Lots of boilerplate setup | Convention over Configuration |

---

# Starting the Container

## Spring Core

```java
AnnotationConfigApplicationContext context =
        new AnnotationConfigApplicationContext(AppConfig.class);
```

You were responsible for:

- Creating the container
- Registering configuration classes
- Refreshing the context
- Closing the context

---

## Spring Boot

```java
@SpringBootApplication
public class MySpringBootApp {

    public static void main(String[] args) {
        SpringApplication.run(MySpringBootApp.class, args);
    }

}
```

`SpringApplication.run()` performs the complete application startup process.

Internally it:

- Creates the `ApplicationContext`
- Prepares the application's `Environment`
- Loads `application.properties`
- Registers Bean Definitions
- Performs Component Scanning
- Imports AutoConfiguration classes
- Executes BeanFactoryPostProcessors
- Creates singleton beans
- Performs dependency injection
- Executes lifecycle callbacks
- Starts the application

---

# @SpringBootApplication

Instead of writing

```java
@Configuration
@ComponentScan
@EnableAutoConfiguration
```

Spring Boot combines them into

```java
@SpringBootApplication
```

Internally it is equivalent to

```java
@Configuration
@ComponentScan
@EnableAutoConfiguration
```

One annotation replaces three commonly used annotations.

---

# @Configuration

## Spring Core

```java
@Configuration
public class AppConfig {

}
```

## Spring Boot

Exactly the same.

`@Configuration` still defines configuration classes and `@Bean` methods.

Spring Boot simply discovers and registers them automatically.

Nothing changes here.

---

# @ComponentScan

## Spring Core

```java
@ComponentScan("com.sagar")
```

You explicitly told Spring where to scan.

---

## Spring Boot

No need to write it.

The package containing the main application class becomes the root package.

Example

```text
com.sagar
│
├── MySpringBootApp
├── service
├── repository
├── config
└── controller
```

Everything inside the root package and its sub-packages is discovered automatically.

---

# Dependency Management

## Spring Core

Every dependency required its own version.

```xml
spring-core
spring-context
spring-beans
spring-aop
```

Managing compatible versions was the developer's responsibility.

---

## Spring Boot

The project inherits from

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
</parent>
```

The Parent POM automatically manages:

- Spring Framework
- Hibernate
- Jackson
- Tomcat
- Logging
- Micrometer
- SnakeYAML
- Hundreds of compatible library versions

Now you simply write

```xml
<dependency>
    ...
</dependency>
```

without specifying versions in most cases.

---

# Starter Dependencies

Instead of adding Spring libraries individually,

```xml
spring-core
spring-context
spring-beans
spring-aop
spring-expression
...
```

Spring Boot provides Starter dependencies.

Example

```xml
spring-boot-starter
```

This single dependency automatically pulls all required Spring Core libraries.

Similarly,

```text
spring-boot-starter-web
spring-boot-starter-data-jpa
spring-boot-starter-security
spring-boot-starter-test
```

each starter brings everything required for that feature.

---

# Auto Configuration

One of Spring Boot's biggest features is Auto Configuration.

Instead of manually configuring infrastructure beans, Spring Boot creates them automatically based on the dependencies available on the classpath.

For example,

```text
spring-boot-starter-data-jpa
```

automatically configures

- DataSource
- Hibernate
- EntityManagerFactory
- TransactionManager
- Repository support

without requiring manual configuration classes.

---

# Where Do These Beans Come From?

Every Spring Boot Starter contributes one or more AutoConfiguration classes.

These are normal Spring configuration classes.

```java
@Configuration
public class JpaAutoConfiguration {

    @Bean
    ...
}
```

Spring Boot imports these configuration classes automatically through

```text
META-INF/spring/
org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

Each line inside this file points to an AutoConfiguration class that Spring Boot should import during startup.

The beans created by Boot are still ordinary Spring beans.

---

# How Does Auto Configuration Decide?

Before creating infrastructure beans, Spring Boot evaluates several conditions.

## @ConditionalOnClass

```text
Required class exists?

YES
 │
 ▼
Configure feature

NO
 │
 ▼
Skip configuration
```

Example

```text
Hibernate present?

↓

Configure JPA
```

If Hibernate is not present on the classpath, Boot skips all JPA configuration.

---

## @ConditionalOnMissingBean

```text
Developer already created bean?

YES
 │
 ▼
Use developer's bean

NO
 │
 ▼
Create Boot's default bean
```

This ensures custom beans always override Spring Boot defaults.

---

# Configuration Files

## Spring Core

```java
@Configuration
@PropertySource("classpath:app.properties")
public class AppConfig {

}
```

Spring would load the file because you explicitly registered it.

---

## Spring Boot

Simply create

```text
src/main/resources/application.properties
```

or

```text
src/main/resources/application.yml
```

Spring Boot automatically discovers and loads them.

No `@PropertySource` is required.

The loaded configuration is stored inside Spring's `Environment`.

Whenever Spring encounters

```java
@Value("${property.name}")
```

it retrieves the value from the `Environment`, converts it to the required Java type if necessary, and injects it into the bean.

---

# BeanFactoryPostProcessor

After all Bean Definitions have been registered, Spring executes every `BeanFactoryPostProcessor`.

Unlike normal beans, these processors work on **Bean Definitions**, not actual bean objects.

Since beans have not been created yet, they can modify metadata before Spring starts instantiating objects.

Typical responsibilities include:

- Registering additional Bean Definitions
- Modifying Bean Definitions
- Processing configuration classes
- Resolving placeholders
- Preparing the BeanFactory

One important implementation is

```text
ConfigurationClassPostProcessor
```

It processes annotations such as:

- `@Configuration`
- `@ComponentScan`
- `@Import`
- `@Bean`

Without it, Spring would never discover configuration classes or `@Bean` methods.

---

# Bean Lifecycle

Spring Boot does **not** change the Spring Bean Lifecycle.

The complete lifecycle remains:

```text
Bean Definition Registered
        │
        ▼
BeanFactoryPostProcessors
        │
        ▼
Instantiate Bean
(Constructor)
        │
        ▼
Dependency Injection
(@Autowired, @Value)
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
Bean Ready
        │
        ▼
@PreDestroy
        │
        ▼
Bean Destroyed
```

Spring Boot simply automates everything that happens before this lifecycle begins.

---

# BeanPostProcessor

Unlike BeanFactoryPostProcessors, BeanPostProcessors work on **actual bean objects**.

Spring executes them twice during bean initialization.

```text
Instantiate Bean
        │
        ▼
Dependency Injection
(@Autowired, @Value)
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
Bean Ready
```

BeanPostProcessors provide many core Spring features including:

- Processing `@Autowired`
- Processing `@Value`
- Supporting `@PostConstruct`
- Creating AOP proxies
- Processing Spring annotations

Some important implementations are:

- `AutowiredAnnotationBeanPostProcessor`
- `CommonAnnotationBeanPostProcessor`
- `AnnotationAwareAspectJAutoProxyCreator`

These infrastructure beans participate in the initialization of every Spring bean.

---

# Convention Over Configuration

Spring Framework required developers to explicitly configure many common things.

Spring Boot follows **Convention Over Configuration**.

Instead of asking developers to configure everything manually, Boot provides sensible defaults.

If required, every default can still be overridden by custom configuration.

---

# Final Picture

```text
Your Application
        │
        ▼
@SpringBootApplication
        │
        ▼
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
Component Scan
        │
        ▼
Import AutoConfiguration Classes
        │
        ▼
Evaluate @Conditional... annotations
        │
        ▼
Register Bean Definitions
        │
        ▼
BeanFactoryPostProcessors
        │
        ▼
Instantiate Beans
        │
        ▼
Dependency Injection
(@Autowired, @Value)
        │
        ▼
Aware Interfaces
        │
        ▼
BeanPostProcessors
        │
        ▼
@PostConstruct
        │
        ▼
ApplicationContext Ready
        │
        ▼
Application Ready
```

Spring Framework still provides the **IoC Container, Dependency Injection, Bean Lifecycle, AOP, BeanFactory, ApplicationContext, BeanFactoryPostProcessor, and BeanPostProcessor**.

Spring Boot builds on top of Spring Framework by automating repetitive configuration, managing dependency versions, providing starter dependencies, automatically loading configuration files, importing AutoConfiguration classes, and creating common infrastructure beans while still allowing developers to override the defaults whenever required.

---

# Creating a Spring Boot Project Without Spring Initializr

Spring Initializr is simply a **project generator**. It does not add any additional Spring Boot features.

Without Spring Initializr, you manually create:

- Project structure
- `pom.xml`
- Main Application Class
- `application.properties`
- Required Starter Dependencies
- Parent POM
- Spring Boot Maven Plugin

Both approaches produce exactly the same Spring Boot application.

---

# Why spring-boot-starter-parent?

The Parent POM is not mandatory, but it is used by almost every Spring Boot project.

It automatically manages:

- Compatible dependency versions
- Maven plugin versions
- Java compiler configuration
- Common Maven defaults

Without it, developers must manually maintain compatible versions.

---

# Why spring-boot-maven-plugin?

The plugin is not required for running the application from an IDE.

However, it becomes important while packaging the application.

Without it,

```bash
mvn package
```

creates a normal JAR.

With the plugin,

```bash
mvn package
```

creates an executable Spring Boot JAR containing:

- Application classes
- Dependency JARs
- Spring Boot Launcher

This allows the application to be executed using

```bash
java -jar myapp.jar
```

without manually configuring the classpath.

The plugin also provides

```bash
mvn spring-boot:run
```

for directly running the application from Maven.

---

# Spring Initializr vs Manual Setup

| Spring Initializr | Manual Setup |
|-------------------|--------------|
| Generates project structure | Create folders manually |
| Adds Parent POM | Add manually |
| Adds Starter Dependencies | Add manually |
| Adds Spring Boot Maven Plugin | Add manually |
| Creates main application class | Create manually |
| Creates `application.properties` | Create manually |
| Ready to run | Configure everything yourself |

Both approaches produce the **same Spring Boot application**.

Spring Initializr simply saves time by generating the standard project structure and Maven configuration automatically.
```
````
