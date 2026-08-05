Here is the complete, updated `README.md` containing all your requested refinements and exact lifecycle updates:

```markdown
# 🌱 Spring Framework – IoC Container & Bean Lifecycle

This project demonstrates what actually happens inside the Spring IoC Container from the moment it is created until it is destroyed.

Instead of focusing on business logic, the project follows the complete lifecycle of a Spring bean and the `ApplicationContext`, showing how Spring discovers beans, creates bean definitions, instantiates objects, performs dependency injection, invokes lifecycle callbacks, and finally destroys beans when the container is closed.

---

# Objectives

This project demonstrates:

* Creation of the Spring IoC Container
* Bean Definition creation
* `BeanFactoryPostProcessor`
* Bean Instantiation
* Dependency Injection
* Aware Interfaces
* `@PostConstruct`
* `@PreDestroy`
* Container Shutdown

---

# Bean Lifecycle Overview

The complete lifecycle demonstrated by this project is:

```text
Application Starts
        │
        ▼
Create ApplicationContext
        │
        ▼
Register Configuration Classes
        │
        ▼
refresh()
        │
        ▼
Scan Components
        │
        ▼
Create Bean Definitions
        │
        ▼
BeanFactoryPostProcessor
        │
        ▼
Create Bean Objects
        │
        ▼
Dependency Injection
        │
        ▼
Aware Interfaces
        │
        ▼
BeanPostProcessor
postProcessBeforeInitialization()
        │
        ▼
@PostConstruct
        │
        ▼
InitializingBean.afterPropertiesSet()
(Optional)
        │
        ▼
Custom init-method
(Optional)
        │
        ▼
BeanPostProcessor
postProcessAfterInitialization()
        │
        ▼
Bean Ready
        │
        ▼
Application Running
        │
        ▼
context.close()
        │
        ▼
@PreDestroy
        │
        ▼
Beans Destroyed
        │
        ▼
ApplicationContext Destroyed

```

---

# Phase 1 — Creating the IoC Container

The project begins by creating an empty `AnnotationConfigApplicationContext`.

At this stage:

* The IoC Container exists.
* BeanFactory exists.
* No beans have been created.
* No component scanning has occurred.

---

# Phase 2 — Registering Configuration

The configuration class is registered with the container.

At this point Spring only knows **where to begin scanning**.

Beans are still **not created**.

---

# Phase 3 — Calling `refresh()`

`refresh()` is the most important method in the Spring lifecycle.

Calling it starts the complete initialization process.

During this phase Spring:

* Reads configuration classes
* Performs component scanning
* Creates Bean Definitions
* Registers internal Spring infrastructure beans
* Executes BeanFactoryPostProcessors
* Creates singleton beans
* Performs Dependency Injection
* Executes lifecycle callbacks

Without calling `refresh()`, the container remains empty.

---

# Phase 4 — Component Scanning

Spring scans the configured package looking for components such as:

* `@Component`
* `@Service`
* `@Repository`
* `@Controller`
* `@Configuration`

For every discovered bean, Spring creates a **Bean Definition**.

A Bean Definition is metadata describing a bean.

It contains information such as:

* Bean name
* Bean class
* Scope
* Lazy initialization
* Constructor information
* Lifecycle metadata

No actual objects are created yet.

---

# Phase 5 — BeanFactoryPostProcessor

Once every Bean Definition has been created, Spring executes every registered `BeanFactoryPostProcessor`.

At this stage:

* Bean Definitions can still be modified.
* Bean objects have **not** been created.

This project prints every Bean Definition to show that Spring already knows every bean before creating any objects.

This phase is commonly used by Spring itself to modify or register additional bean definitions before instantiation begins.

---

# Phase 6 — Bean Creation

After Bean Definitions are finalized, Spring begins creating singleton beans.

Constructors are invoked.

Objects now begin to exist inside the IoC Container.

---

# Phase 7 — Dependency Injection

After creating a bean, Spring resolves all required dependencies.

Constructor Injection, Setter Injection, or Field Injection occurs during this phase.

Only after dependencies are injected can the bean safely interact with other beans.

---

# Phase 8 — Aware Interfaces

If a bean implements one of Spring's Aware interfaces, Spring supplies additional framework information.

Examples include:

* `BeanNameAware`
* `ApplicationContextAware`
* `EnvironmentAware`
* `BeanFactoryAware`

These callbacks allow beans to access Spring infrastructure when required.

---

# Phase 9 — BeanPostProcessors

After dependency injection and Aware callbacks, Spring executes every registered `BeanPostProcessor`.

A `BeanPostProcessor` is itself a **Spring-managed Bean**.

Developers can create their own BeanPostProcessors by implementing:

```java
BeanPostProcessor

```

and registering it as a Spring Bean using `@Component` or `@Bean`.

Spring automatically calls every registered BeanPostProcessor for **every bean** in the container.

There are two callback methods.

### postProcessBeforeInitialization()

Executed before initialization callbacks such as:

* `@PostConstruct`
* `InitializingBean.afterPropertiesSet()`
* Custom init-method

This callback allows Spring or developers to inspect or modify a bean before it is fully initialized.

---

### postProcessAfterInitialization()

Executed after all initialization callbacks have completed.

This is one of the most important extension points in Spring.

At this stage Spring can:

* Wrap beans
* Replace beans
* Return Proxy objects instead of the original bean

This is the place where many Spring features are implemented, including:

* Spring AOP
* `@Transactional`
* Lazy Proxies
* Security Proxies
* Other framework-generated proxy objects

In many cases the object stored inside the ApplicationContext is **not the original bean**, but a proxy created during `postProcessAfterInitialization()`.

Because of this mechanism, Spring can transparently add additional behavior around your methods without modifying your original class.

BeanPostProcessors intercept every bean during initialization and are one of the primary extension points of the Spring Framework.

---

# Phase 10 — `@PostConstruct` & Initialization

After `BeanPostProcessor.postProcessBeforeInitialization()` completes, Spring invokes methods annotated with `@PostConstruct`.

If the bean implements `InitializingBean`, Spring then calls:

* `afterPropertiesSet()`

If a custom init-method is configured, Spring invokes it next.

Only after all initialization callbacks finish does Spring execute:

* `BeanPostProcessor.postProcessAfterInitialization()`

Once this phase completes, the bean is considered fully initialized and ready for use.

---

# Phase 11 — Bean Ready

At this point:

* Bean creation has completed.
* Dependencies are available.
* Initialization logic has executed.

The bean is now managed by the Spring IoC Container and can safely be used by the application.

---

# Phase 12 — Container Shutdown

When the application closes,

```java
context.close();

```

Spring begins shutting down the IoC Container.

Before removing beans, Spring executes lifecycle callbacks responsible for cleanup.

---

# Phase 13 — `@PreDestroy`

Methods annotated with `@PreDestroy` execute before bean destruction.

Typical responsibilities include:

* Closing files
* Releasing database connections
* Closing sockets
* Stopping threads
* Clearing resources
* Performing cleanup operations

After cleanup completes, Spring destroys the bean and removes it from the container.

---

# Internal Spring Infrastructure

During startup, Spring automatically registers several internal infrastructure beans.

Some important ones visible in this project are:

| Bean | Responsibility |
| --- | --- |
| `ConfigurationClassPostProcessor` | Processes `@Configuration`, `@ComponentScan`, and `@Bean` methods |
| `AutowiredAnnotationBeanPostProcessor` | Performs dependency injection for `@Autowired` and constructor injection |
| `CommonAnnotationBeanPostProcessor` | Processes annotations such as `@PostConstruct`, `@PreDestroy`, and `@Resource` |
| `EventListenerMethodProcessor` | Registers methods annotated with `@EventListener` |
| `DefaultEventListenerFactory` | Creates event listener instances |

These are framework beans automatically registered by Spring before your own application beans are created.

---

# Key Learnings

* Creating an `ApplicationContext` does **not** immediately create beans.
* `refresh()` is responsible for initializing the entire IoC Container.
* Spring first creates **Bean Definitions**, then creates bean objects.
* `BeanFactoryPostProcessor` works on Bean Definitions before bean instantiation.
* Dependency Injection occurs before initialization callbacks.
* Aware interfaces provide beans with Spring infrastructure information.
* `BeanPostProcessor` is itself a Spring-managed bean and intercepts every bean during initialization.
* `postProcessBeforeInitialization()` executes before `@PostConstruct`.
* `postProcessAfterInitialization()` executes after initialization completes.
* Spring frequently replaces the original bean with a proxy during `postProcessAfterInitialization()`.
* Features such as Spring AOP, `@Transactional`, lazy initialization, and security proxies are implemented using BeanPostProcessors.
* `@PostConstruct` executes after dependency injection and before the bean is used.
* `@PreDestroy` executes during container shutdown before bean destruction.
* Closing the `ApplicationContext` triggers the complete bean destruction lifecycle.

This project provides a complete view of the **birth, initialization, usage, and destruction of Spring-managed beans**, helping build a strong understanding of the internal lifecycle of the Spring IoC Container.

```

```