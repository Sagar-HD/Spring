# Spring Profiles & Environment Variables Demo

## 🚀 Overview

This project demonstrates how **Spring Profiles**, **Properties Files**, and **Environment Variables** work together to control application behavior across different environments like **dev** and **prod**.

The focus is on understanding **how Spring internally resolves configuration** rather than just using it blindly.

---

## 🧠 What You’ll Learn

* What **Environment** means in Spring (from first principles)
* How Spring decides **which beans to load using @Profile**
* How **property resolution actually works (priority-based lookup)**
* How **environment variables override configuration**
* The difference between **Spring (core)** and **Spring Boot behavior**

---

## 🧩 Project Structure

```text
com.sagar
├── config
│   └── AppConfig.java
├── controller
│   └── PaymentController.java
├── service
│   ├── PaymentService.java
│   ├── DummyPaymentService.java   (@Profile: dev, default)
│   └── UpiPaymentService.java     (@Profile: prod)
└── App.java
```

---

## ⚙️ Core Concepts (First Principles)

### 🔹 What is Environment?

In Spring:

```text
Environment = PropertySources + ActiveProfiles
```

It holds:

* OS environment variables (`System.getenv`)
* JVM system properties
* Properties files (`application.properties`)
* Active profiles

---

## ⚙️ How It Works Internally

### 🔥 Actual Execution Flow

```text
1. Start Application
2. Create Environment
      → Load System Properties (JVM)
      → Load Environment Variables (OS)
3. Set Active Profile (manually in this project)
4. Load @PropertySource (application.properties)
5. Create Beans
      → Check @Profile condition
      → Load only matching beans
6. Inject values using @Value
      → Resolve property from Environment (priority order)
7. Run Application
```

---

## 🧩 1. Profiles Control Bean Loading

```java
@Service
@Profile("prod")
public class UpiPaymentService implements PaymentService { }
```

```java
@Service
@Profile({"dev", "default"})
public class DummyPaymentService implements PaymentService { }
```

👉 Spring checks active profile during bean creation:

```text
If profile matches → Bean created
Else → Bean skipped
```

---

## 🧩 2. Properties Inject Values

```properties
server.app.mode=default
```

```java
@Value("${server.app.mode}")
private String mode;
```

👉 Value is NOT directly taken from file
👉 It is resolved from **Environment**

---

## 🔥 3. Property Resolution (MOST IMPORTANT)

Spring does NOT merge files manually.

Instead, it searches in **priority order**:

```text
1. JVM System Properties      (highest)
2. Environment Variables      (OS)
3. application.properties     (loaded via @PropertySource)
```

👉 First match wins.

---

## 🧪 Example

```properties
server.app.mode=default
```

Environment variable:

```bash
SERVER_APP_MODE=prod
```

👉 Final value:

```text
prod   (env variable overrides properties file)
```

---

## ⚠️ Important Clarification

In this project (**pure Spring, NOT Boot**):

* ❌ `application.properties` is NOT auto-loaded
* ❌ `spring.profiles.active` inside properties is ignored
* ❌ No automatic loading of `application-prod.properties`

✔ You must manually:

```java
context.getEnvironment().setActiveProfiles("prod");
```

```java
@PropertySource("classpath:application.properties")
```

---

## 🧪 Example Runs

### ▶️ DEV Mode

```text
Active Profile: dev
```

Output:

```text
running in mode : default
Payed using Dummy
```

---

### ▶️ PROD Mode

```text
Active Profile: prod
```

Output:

```text
running in mode : default
Payed using Upi
```

---

## 🧠 Key Differences: Spring vs Spring Boot

| Feature                                     | Spring (Core) | Spring Boot |
| ------------------------------------------- | ------------- | ----------- |
| Auto-load properties                        | ❌             | ✅           |
| Profile activation from file                | ❌             | ✅           |
| Profile-specific files (`application-prod`) | ❌             | ✅           |
| Environment handling                        | Manual        | Automatic   |

---

## 🌍 Real-World Use Cases

* Switching **database configs** (local vs cloud)
* Using **dummy vs real services**
* Managing **API keys & secrets**
* Controlling **logging levels**
* Feature toggles per environment

---

## 🧠 Mental Model (Lock This)

```text
Profiles → WHICH beans are loaded
Properties → WHAT values are used
Environment → WHICH value wins
```

---

## 🔥 Where Overriding Actually Happens

Overriding does NOT happen while loading files.

👉 It happens during **property lookup**:

```text
Spring checks sources in order → returns first match
```

---

## 🚀 Next Steps

* Convert this project to **Spring Boot**
* Use `application-dev.properties` and `application-prod.properties`
* Replace `@Value` with `@ConfigurationProperties`
* Try using **Docker environment variables**
* Add DB configs per profile

---

## 📌 Final Thought

This project helps you understand how configuration works at a **core framework level**.

Once you get this:

* Spring Boot becomes easy
* Microservices config becomes clear
* Production systems make sense

👉 This is foundational backend knowledge.
