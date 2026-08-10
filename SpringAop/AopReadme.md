
---

# 🧩 Spring AOP — Complete Mental Model + Practical Guide

---

## 🚀 Why AOP Exists

In real applications, not all logic is business logic.

### 🧱 Two Types of Logic

| Type                     | Nature                              |
| ------------------------ | ----------------------------------- |
| **Business Logic**       | Vertical (specific to each service) |
| **Infrastructure Logic** | Horizontal (used everywhere)        |

### 🔁 Examples of Cross-Cutting Concerns

* Logging
* Security / Authorization
* Transactions
* Caching
* Performance tracking

---

## ❌ Problems Without AOP

### 1. Scattering

Same logic repeated across multiple methods

### 2. Tangling

Mixing business + infra logic in one place

```java
public void placeOrder() {
    log.info("start");
    checkAuth();
    // business logic
    log.info("end");
}
```

### 3. Breaks Clean Architecture

Even utility methods don’t solve it — services become bloated

---

## ✅ AOP Solution

👉 Extract common logic
👉 Apply it automatically **without touching business code**

---

## 🧠 Core Idea

AOP answers 3 questions:

| Question  | Meaning         |
| --------- | --------------- |
| **What**  | Logic to apply  |
| **When**  | When to execute |
| **Where** | Which methods   |

---

## 🔑 Core Concepts

### 🔹 Aspect

Class containing cross-cutting logic

```java
@Aspect
@Component
class LoggingAspect {}
```

---

### 🔹 Advice = (What + When)

Types:

* `@Before`
* `@After`
* `@AfterReturning`
* `@AfterThrowing`
* `@Around`

---

### 🔹 Pointcut = Where

Defines **which methods** are intercepted

---

### 🔹 Join Point

Actual method execution point

---

### 🔹 Weaving

Applying aspects at runtime using proxies

---

## ⚙️ How Spring AOP Works

👉 Uses **Proxy Pattern**

* Proxy sits between caller and real object
* Intercepts method calls
* Executes advice

---

## 🔁 Advice Flow (Mental Model)

```java
before();

try {
    result = method();

    afterReturning(result);
    return result;

} catch (ex) {
    afterThrowing(ex);
    throw ex;

} finally {
    after();
}
```

---

## 🔍 Advice Types Deep Dive

### 🔹 `@Before`

* Runs before method
* Access args via `JoinPoint`
* ❌ Cannot modify args
* ❌ Cannot stop execution (except exception)

---

### 🔹 `@AfterReturning`

* Runs only on success
* Access return value
* ❌ Cannot replace return value
* ✅ Can mutate returned object

---

### 🔹 `@AfterThrowing`

* Runs on exception
* Gets exception object
* ❌ Cannot stop propagation

---

### 🔹 `@After`

* Always runs (like `finally`)

---

### 🔹 `@Around` 🔥

* Full control
* Modify args, return, exceptions
* Decide whether to call method

```java
@Around("pointcut()")
public Object around(ProceedingJoinPoint pjp) throws Throwable {
    Object result = pjp.proceed();
    return result;
}
```

---

## ⚠️ Important AOP Behavior

* Works only on **Spring Beans**
* Works at **method level**
* Internal method calls ❌ NOT intercepted (proxy limitation)

---


---

## 🔢 When Multiple Aspects Exist

Assume:

* `AspectA @Order(1)`
* `AspectB @Order(2)`

---

## 🟢 `@Before`

👉 Runs **before method**

```
AspectA → AspectB → Method
```

✔ Lower order runs first

---

## 🔴 `@After`

👉 Runs **after method**

```
Method → AspectB → AspectA
```

✔ Reverse order

---

## 💀 `@Around`

👉 Wraps method (like layers)

```
AspectA (before)
  AspectB (before)
    Method
  AspectB (after)
AspectA (after)
```

✔ Nested execution (onion model)

👉 **One-liner:** Around = you control both before & after using `proceed()`

---

## 🧠 One Line Memory

* Before = top → down
* After = bottom → up
* Around = wrap (inside-out)



# 🎯 POINTCUT EXPRESSIONS (VERY IMPORTANT)

## 🧠 Execution Designator

```java
execution(access-modifier return-type package.Class.method(args) throws-exception)
```

### ✅ Mandatory:

* return-type
* class + method
* args

### ❌ Optional:

* access modifier
* exception

---

## ⭐ Wildcards

### `*` → Matches one level

| Usage        | Meaning                   |
| ------------ | ------------------------- |
| `*` (return) | any return type           |
| `*` (method) | any method                |
| `get*`       | methods starting with get |
| `*Service`   | class ending with Service |
| `(*, *)`     | exactly 2 args            |

---

### `..` → Matches multiple levels

| Usage       | Meaning               |
| ----------- | --------------------- |
| `(..)`      | any number of args    |
| `com.app..` | package + subpackages |

---

## 🔥 Examples

```java
execution(* com.app.service.*.*(..))
```

👉 All methods in service package

```java
execution(* *.*(..))
```

👉 Everything (dangerous)

---

## ⚠️ Important Note

Too broad expressions may break because:

* Spring internal beans
* Proxy limitations

---

# 🧩 Other Pointcut Designators

### 🔹 `within`

Class-level matching

```java
within(com.app.service.*)
```

---

### 🔹 `@annotation`

Intercept methods with annotation

```java
@annotation(TrackTime)
```

---

### 🔹 `bean`

By Spring bean name

```java
bean(userService)
```

---

### 🔹 Logical Operators

```java
execution(...) && @annotation(...)
execution(...) || within(...)
!execution(...)
```

---

## 🏷️ Named Pointcuts (Best Practice)

```java
@Pointcut("execution(* com.app.service.*.*(..))")
public void serviceLayer() {}
```

Use:

```java
@Before("serviceLayer()")
```

---

## 📦 Centralized Pointcuts

```java
class AppPointcuts {
    @Pointcut("execution(* com.app.service.*.*(..))")
    public void serviceLayer(){}
}
```

---

# 🧠 Less Used Designators (Know, don’t memorize)

| Designator | Meaning                                   |
| ---------- | ----------------------------------------- |
| `@within`  | class annotation (no inheritance support) |
| `@target`  | supports inheritance                      |
| `args`     | runtime argument types                    |
| `@args`    | annotation on args                        |
| `this`     | proxy type                                |
| `target`   | actual object type                        |

---

# ⚙️ PROXY TYPES (VERY IMPORTANT)

Spring AOP uses 2 types:

---

## 🔹 1. CGLIB Proxy (Default)

* Works without interface
* Creates subclass using inheritance
* Calls `super.method()`

### 🔥 Key Point:

👉 `this == target`

---

## 🔹 2. JDK Dynamic Proxy

* Requires interface
* Proxy implements interface
* Calls actual target object

### 🔥 Key Point:

👉 `this != target`

---

## ⚠️ Difference in Pointcuts

| Keyword  | Meaning        |
| -------- | -------------- |
| `this`   | proxy instance |
| `target` | real object    |

👉 With JDK proxy:

* `this` → interface
* `target` → actual class

---

## ⚙️ Forcing JDK Proxy

```properties
spring.aop.proxy-target-class=false
```

(Default is `true` → CGLIB)

---

## 💡 When to Use JDK Proxy?

* When using interfaces
* For loose coupling across services

---

# 🧠 FINAL MENTAL MODEL

👉 AOP = Intercept method calls using proxy and inject logic around execution

---

## 🔥 One-Liner

> “AOP separates cross-cutting concerns by applying logic around method execution using proxies without modifying business code.”

---

# ⚙️ INTERNAL FLOW — WHEN & HOW PROXIES ARE CREATED

## 🧠 Big Picture

AOP is not “magic at runtime randomly”

👉 Most of the heavy work happens **during application startup**

---

## 🔄 Step-by-Step Flow

### 1. Spring Starts & Scans Beans

* All `@Component`, `@Service`, `@Aspect` classes detected
* Beans are created normally

---

### 2. Aspect Beans Are Identified

* Classes annotated with `@Aspect`
* Spring extracts:

    * Advice methods (`@Before`, `@Around`, etc.)
    * Pointcut expressions

👉 Stores internally as:

```
(Pointcut Expression) → (Advice Method Mapping)
```

---

### 3. BeanPostProcessor Kicks In 🔥

Spring uses:

👉 `AnnotationAwareAspectJAutoProxyCreator`

This is a **BeanPostProcessor**

---

## 🧩 Bean Lifecycle Hook

```java
Bean Creation →
    postProcessBeforeInitialization →
    Initialization →
    postProcessAfterInitialization → 🔥 AOP Proxy Created Here
```

---

### 4. Proxy Creation (IMPORTANT)

During:

👉 `postProcessAfterInitialization`

Spring checks:

* Does this bean match any pointcut?

### If YES:

👉 Wraps original bean inside a proxy

### If NO:

👉 Returns original bean

---

## 🔁 What Proxy Contains

Proxy internally has:

* Target object (actual bean)
* List of advices
* Method → advice mapping

---

## ⚡ Runtime Flow (When Method is Called)

```java
Client → Proxy → (Advice Chain) → Target Method
```

Steps:

1. Method called on proxy
2. Proxy checks mapping
3. Executes:

    * Before advice
    * Around advice (if present)
4. Calls actual method
5. Executes after/exception logic

---

## ⚠️ Important Internal Behavior

* Mapping is **prepared at startup**
* Matching is **not recomputed every time**
* That’s why AOP is efficient

---

## 🚨 Why Internal Method Calls Fail

```java
this.methodB();
```

👉 Direct call → skips proxy
👉 So AOP is NOT applied

---

# 🧩 CUSTOM ANNOTATIONS (FOR AOP)

## 🧠 Why?

Instead of writing complex pointcuts:

```java
execution(* com.app.service.*.*(..))
```

👉 We can mark methods explicitly

---

## ✨ Step 1: Create Annotation

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TrackTime {

    String value() default "";

    int threshold() default 0;
}
```

---

## 🧠 Annotation Concepts

### 🔹 `@Retention`

| Type      | Meaning                                 |
| --------- | --------------------------------------- |
| `SOURCE`  | Removed at compile                      |
| `CLASS`   | In .class file                          |
| `RUNTIME` | Available at runtime (REQUIRED for AOP) |

👉 Always use:

```java
@Retention(RetentionPolicy.RUNTIME)
```

---

### 🔹 `@Target`

Where annotation can be used:

| Type        | Meaning      |
| ----------- | ------------ |
| `TYPE`      | class        |
| `METHOD`    | method       |
| `FIELD`     | variable     |
| `PARAMETER` | method param |

👉 For AOP mostly:

```java
@Target(ElementType.METHOD)
```

---

## 🧩 Step 2: Use Annotation

```java
@TrackTime(value = "order", threshold = 100)
public void placeOrder() {}
```

---

## 🧩 Step 3: Use in Aspect

```java
@Around("@annotation(trackTime)")
public Object trackExecution(ProceedingJoinPoint pjp, TrackTime trackTime) throws Throwable {

    long start = System.currentTimeMillis();

    Object result = pjp.proceed();

    long end = System.currentTimeMillis();

    System.out.println(trackTime.value() + " took " + (end - start));

    return result;
}
```

---

## 🔥 What You Get

* Clean code
* No complex expressions
* Fully customizable logic

---

## ⚡ Advanced Notes

* Annotation values can drive behavior
* Can combine with other pointcuts
* Used heavily in:

    * `@Transactional`
    * `@Cacheable`
    * `@Async`

---

# 🧠 FINAL EXTENDED MENTAL MODEL

👉 At startup:

* Spring scans aspects
* Builds mapping
* Wraps beans with proxies

👉 At runtime:

* Proxy intercepts method
* Applies advice chain
* Calls real method

---

## 🔥 Ultimate One-Liner

> “Spring AOP creates proxies at startup using BeanPostProcessors, maps pointcuts to advice, and intercepts method calls at runtime to apply cross-cutting logic.”

---








