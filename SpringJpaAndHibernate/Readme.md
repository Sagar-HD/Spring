
# 🧠 Spring Data JPA + Hibernate + `@Transactional`

## From First Principles → Runtime Flow → Internals → Mental Model

> **Purpose:** This README is not meant to make you memorize Hibernate APIs.
> It is a mental map of **what actually happens when a request enters your Spring Boot application and eventually changes a database row**.
>
> The goal is to make JPA + Hibernate feel as natural as the JDBC flow you already understand.

---

# 📚 0. Where We Came From

Before ORM, we had:

```text
Java Object
     ↓
JDBC / JdbcTemplate
     ↓
SQL
     ↓
Database
```

With Spring `JdbcTemplate`, a lot of JDBC boilerplate disappeared:

```text
Connection handling
PreparedStatement handling
ResultSet iteration
Exception translation
Resource cleanup
```

But we still had to manually solve the **object ↔ relational mapping problem**.

For example:

```text
DB row
────────────────
id | name | age
────────────────
 ↓
manually create
 ↓
Student object
```

And:

```text
Student object
      ↓
manually extract fields
      ↓
SQL parameters
```

ORM solves this.

```text
             ORM
              │
      ┌───────┴───────┐
      ↓               ↓
 Java Objects      DB Rows
```

JPA + Hibernate takes this much further by managing **entity identity, lifecycle, persistence context, dirty checking, flushing, relationships, SQL generation, etc.**

---

# 🏛️ 1. JPA vs Hibernate

This distinction must stay clear.

## JPA

JPA is a **standard/specification/API**.

It defines concepts such as:

```text
@Entity
@Id
EntityManager
Persistence Context
Entity lifecycle
Mappings
Relationships
JPQL
```

Example:

```java
entityManager.find(Student.class, 1L);
```

`EntityManager` is a JPA API.

---

## Hibernate

Hibernate is the **implementation/provider**.

Conceptually:

```text
Your code
   ↓
JPA EntityManager API
   ↓
Hibernate implementation
   ↓
JDBC
   ↓
Database
```

So:

> **JPA defines the contract. Hibernate implements the persistence behavior.**

---


---

# 🔄 1A. Plain Java + JPA + Hibernate — NO SPRING

Before introducing Spring, let's use JPA + Hibernate exactly as a normal Java application.

There is:

❌ No Spring Container  
❌ No `@Service`  
❌ No `@Repository`  
❌ No `@Transactional`  
❌ No Spring `TransactionManager`  
❌ No Spring AOP

Only:

```text
Your Java Code
      ↓
JPA API
      ↓
Hibernate
      ↓
JDBC
      ↓
Database
```

This stage is extremely important because it lets us see:

> **What JPA/Hibernate itself is responsible for before Spring starts managing it.**

---

## 🧩 1A.1 Dependencies

Conceptually we need:

```text
jakarta.persistence-api
hibernate-core
mysql-connector-j
```

Their responsibilities:

| Dependency | Responsibility |
|---|---|
| `jakarta.persistence-api` | JPA interfaces and annotations |
| `hibernate-core` | Hibernate implementation/provider |
| `mysql-connector-j` | JDBC driver for MySQL |

So:

```text
Your Code
   ↓
JPA API
   ↓
Hibernate
   ↓
MySQL JDBC Driver
   ↓
MySQL
```

> **Note:** The exact dependency versions belong to the chosen Java/Hibernate/JDBC-driver stack. This README intentionally keeps them conceptual rather than pinning versions.

---

# 🏗️ 1A.2 There Is NO Spring Container

In this stage, **you are the container** 😄.

Normally Spring would create and manage things such as:

```text
EntityManagerFactory
EntityManager
TransactionManager
DataSource
Repository beans
Service beans
```

But here:

```text
YOU
 │
 ├── create EntityManagerFactory
 │
 ├── create EntityManager
 │
 ├── begin transaction
 │
 ├── perform persistence operations
 │
 ├── commit / rollback
 │
 └── close resources
```

This is the first important transition.

---

# 🏭 1A.3 EntityManagerFactory

First we need an:

```text
EntityManagerFactory
```

Conceptually:

```java
EntityManagerFactory emf =
        Persistence.createEntityManagerFactory("myPersistenceUnit");
```

Think of it as:

> A long-lived factory from which EntityManagers are created.

```text
Application
     │
     ▼
EntityManagerFactory
     │
     ├────→ EntityManager 1
     │
     ├────→ EntityManager 2
     │
     └────→ EntityManager 3
```

The factory is expensive/long-lived.

You normally create **one factory for the application**.

The persistence-unit name and persistence configuration are part of the JPA bootstrap configuration. The important mental model here is the API call above; we are intentionally not introducing XML configuration in this learning path.

---

# 🧑‍💻 1A.4 Creating EntityManager

When you need a persistence context:

```java
EntityManager em =
        emf.createEntityManager();
```

Now:

```text
EntityManager
      │
      ▼
Persistence Context
      │
      ├── managed Entity A
      ├── managed Entity B
      └── managed Entity C
```

Remember:

> **The EntityManager gives us access to the persistence context.**

The persistence context manages entity state.

---

# 🔄 1A.5 Transaction — YOU Manage It

There is no Spring `@Transactional`.

So you explicitly do:

```java
EntityTransaction tx = em.getTransaction();

try {

    tx.begin();

    em.persist(student);

    tx.commit();

} catch (Exception e) {

    tx.rollback();

}
```

The important thing is:

```text
YOU
 │
 ▼
EntityTransaction
 │
 ▼
JPA
 │
 ▼
Hibernate
 │
 ▼
Database transaction
```

At this stage, transaction demarcation is your responsibility.


---
# 🧠 1A.5A Why Hibernate Needs a Unit of Work

At this point, we already know the JDBC transaction:

```text
JDBC Connection
      ↓
setAutoCommit(false)
      ↓
SQL statements
      ↓
commit / rollback

```

So a natural question is:

> **If the JDBC Connection already has a transaction, why does Hibernate need its own transaction / unit-of-work concept?**

Because a JDBC transaction understands SQL and database state.

Hibernate has to manage a much higher-level world:

```text
Java Objects
     ↓
Entity Lifecycle
     ↓
Persistence Context
     ↓
Managed Entities
     ↓
Dirty Checking
     ↓
Flush
     ↓
SQL generation
     ↓
JDBC
     ↓
Database

```

### JDBC does not understand:

* `Student` object is **MANAGED**
* `Student`'s original snapshot
* `Student` was modified
* Entity should be **INSERTed**
* Entity should be **UPDATEd**
* Entity should be **DELETEd**

**Hibernate does.**

Therefore, Hibernate needs a unit of work in which it can keep track of entity state and eventually synchronize that state with the database.

---

## 🔗 Hibernate Unit of Work → JDBC Transaction

The important thing is:

* Hibernate's unit of work is **NOT** a second independent database transaction.
* Think of it as a higher-level persistence layer that eventually uses the underlying JDBC/database transaction.

```text
        HIBERNATE / JPA
        ────────────────
        Unit of Work
             │
             ├── EntityManager
             ├── Persistence Context
             ├── Managed entities
             ├── Dirty Checking
             └── Flush
                    │
                    ▼
                  JDBC
                    │
                    ▼
            JDBC Connection
                    │
                    ▼
          Database Transaction
                    │
             ┌──────┴──────┐
             ▼             ▼
          COMMIT        ROLLBACK

```

So the layers are:

1. **Hibernate/JPA**
   ↓ *"Manage my Java persistence work"*
2. **JDBC**
   ↓ *"Send SQL through a Connection"*
3. **Database**
   ↓ *"Actually provide transactional guarantees"*

The physical database transaction still happens underneath. Hibernate simply manages the object-level unit of work that produces the SQL participating in that transaction.

---

## 🔄 Why This Abstraction Is Useful

Suppose:

```java
Student s = entityManager.find(Student.class, 1L);
s.setName("Sagar");

Order order = entityManager.find(Order.class, 10L);
order.setAmount(5000);

```

JDBC only eventually sees something like:

```sql
UPDATE student SET name = 'Sagar' WHERE id = 1;
UPDATE orders SET amount = 5000 WHERE id = 10;

```

But Hibernate knows:

* `Student` → **MANAGED** → modified
* `Order` → **MANAGED** → modified

It can therefore execute:

```text
Persistence Context
        ↓
Dirty Checking
        ↓
Flush
        ↓
Generate SQL
        ↓
JDBC Connection
        ↓
Database Transaction

```

This is why Hibernate needs a higher-level persistence/unit-of-work abstraction even though the actual database transaction ultimately happens through JDBC.

---

## 🚀 And Then Spring Takes Over the Transaction Boundary

In plain JPA + Hibernate, you manage the transaction manually:

```java
EntityTransaction tx = em.getTransaction();
tx.begin();

em.persist(student);

tx.commit();

```

Later, when Spring is introduced, you no longer manually manage this boundary:

```java
@Transactional
public void createStudent(Student student) {
    entityManager.persist(student);
}

```

Spring adds another abstraction above JPA/Hibernate:

```text
              Spring
                │
          @Transactional
                ↓
        TransactionManager
                ↓
        JPA / Hibernate
                ↓
        Persistence Context
                ↓
             Flush
                ↓
        JDBC Connection
                ↓
      Database Transaction

```

Spring does **not** replace Hibernate's unit of work or the underlying JDBC transaction. It takes over transaction demarcation and coordination around the JPA/Hibernate persistence work.

### The Evolution:

#### PLAIN JPA + HIBERNATE

```text
You
 ↓
EntityTransaction
 ↓
Hibernate persistence work
 ↓
JDBC
 ↓
Database Transaction

```

#### SPRING + JPA + HIBERNATE

```text
Spring @Transactional
 ↓
TransactionManager
 ↓
JPA / Hibernate persistence work
 ↓
JDBC
 ↓
Database Transaction

```







---

# 🔥 1A.6 What Happens During `persist()`?

Suppose:

```java
Student student = new Student();

student.setName("Sagar");

em.persist(student);
```

Conceptually:

```text
Student object
      │
      ▼
EntityManager
      │
      ▼
Hibernate
      │
      ▼
Persistence Context
      │
      ▼
Student becomes MANAGED
```

At this point:

> Hibernate does **not necessarily immediately execute `INSERT`**.

The entity is first registered with the Persistence Context.

Later:

```text
flush
   ↓
Hibernate generates INSERT
   ↓
JDBC
   ↓
Database
```

---

# 🧠 1A.7 The Persistence Context Still Exists Without Spring

This is extremely important.

Persistence Context is **not a Spring feature**.

It comes from JPA/Hibernate.

Without Spring:

```text
EntityManager
      ↓
Persistence Context
      ↓
Managed entities
```

With Spring:

```text
Spring
   ↓
transaction-associated EntityManager
   ↓
Persistence Context
```

Spring changes **how the EntityManager/transaction lifecycle is managed**.

It doesn't invent the Persistence Context.

---

# 🔎 1A.8 `find()` Without Spring

```java
Student student =
        em.find(Student.class, 1L);
```

Conceptually:

```text
EntityManager
      ↓
Hibernate
      ↓
Persistence Context
      ↓
Is Student#1 already managed?
       │
    ┌──┴──┐
   YES    NO
    │      │
    │      ▼
    │    JDBC
    │      ↓
    │   Database
    │      ↓
    │   Student
    │      ↓
    │   Persistence Context
    │
    └──────────┐
               ▼
          Managed object
```

So all the Hibernate concepts you've already learned are present **even without Spring**.

---

# 🔌 1A.9 Who Gets the Database Connection?

At this stage, there is no Spring `DataSource` abstraction managing everything for us.

Hibernate ultimately needs JDBC resources.

Conceptually:

```text
Hibernate
    ↓
JDBC connection infrastructure
    ↓
JDBC Driver
    ↓
Connection
    ↓
Database
```

The important distinction:

```text
JPA/Hibernate
    ↓
Persistence Context
    ↓
Entity state

JDBC
    ↓
Connection
    ↓
Physical database communication
```

These are different responsibilities.

---

# 🧹 1A.10 YOU Also Manage the Lifecycle

A simplified application might look like:

```java
EntityManagerFactory emf =
        Persistence.createEntityManagerFactory("myPersistenceUnit");

EntityManager em =
        emf.createEntityManager();

EntityTransaction tx =
        em.getTransaction();

try {

    tx.begin();

    // JPA operations
    em.persist(student);

    tx.commit();

} catch (Exception e) {

    tx.rollback();

} finally {

    em.close();

}

emf.close();
```

Notice how much lifecycle management **you** are doing.

---

# 🧠 1A.11 Plain JPA + Hibernate Mental Model

At this stage:

```text
                    YOUR JAVA CODE
                          │
                          ▼
                  EntityManagerFactory
                          │
                          ▼
                     EntityManager
                          │
                          ▼
                 Persistence Context
                          │
                          ▼
                       Hibernate
                          │
                          ▼
                         JDBC
                          │
                          ▼
                      Database
```

And transaction:

```text
EntityManager
      │
      ▼
EntityTransaction
      │
 ┌────┴────┐
 ▼         ▼
begin()   commit()
           │
           └── rollback() on failure
```

---

# 🚀 1A.12 What Is Missing?

Now look at this carefully.

We have:

```text
JPA + Hibernate
```

but we DON'T have:

```text
Spring Container
Spring Beans
Spring AOP
@Transactional
TransactionManager
TransactionSynchronizationManager
Spring Data
```

So every transaction is manually controlled.

Every EntityManager lifecycle is manually controlled.

Application infrastructure is manually controlled.

This is exactly where the next stage becomes useful.

---

# 🔥 1A.13 The Problem We Have Created

Imagine a real application.

You have:

```text
StudentService
StudentRepository
CourseService
CourseRepository
OrderService
OrderRepository
```

Without Spring, you may repeatedly need to deal with:

```text
create EntityManager
      ↓
begin transaction
      ↓
perform JPA operations
      ↓
commit
      ↓
rollback on exception
      ↓
close EntityManager
```

That becomes repetitive.

And we haven't even introduced:

```text
Dependency Injection
AOP
Transaction boundaries
Resource management
```

This is the exact problem Spring will start solving.

---

# 🔄 1A.14 The Important Transition

We now move from:

```text
                    NO SPRING

YOU
 │
 ├── EntityManagerFactory
 ├── EntityManager
 ├── EntityTransaction
 ├── begin()
 ├── commit()
 ├── rollback()
 └── close()
```

to:

```text
                    SPRING

Spring Container
      │
      ├── EntityManagerFactory
      ├── EntityManager access/lifecycle
      ├── TransactionManager
      └── DataSource

             +

       @Transactional
             ↓
        Spring AOP
             ↓
      Transaction handling
```

The important idea:

> **Spring does not replace JPA or Hibernate. Spring takes over much of the infrastructure and lifecycle management around them.**

---

# 🧭 1A.15 The Evolution So Far

```text
NORMAL JAVA + JDBC
       │
       │ Manual JDBC
       ▼
┌───────────────────────────┐
│ Connection                │
│ PreparedStatement         │
│ ResultSet                 │
│ SQL                       │
│ Transaction               │
└─────────────┬─────────────┘
              │
              │ ORM
              ▼
┌───────────────────────────┐
│ JPA + HIBERNATE           │
│                           │
│ Entity                    │
│ EntityManager             │
│ Persistence Context       │
│ Dirty Checking            │
│ Flush                     │
│ EntityTransaction         │
│                           │
│ ❌ No Spring              │
└─────────────┬─────────────┘
              │
              │ Spring takes
              │ infrastructure
              │ management
              ▼
┌───────────────────────────┐
│ SPRING + JPA + HIBERNATE  │
│                           │
│ Spring Container          │
│ DataSource                │
│ EntityManagerFactory      │
│ JpaTransactionManager     │
│ @Transactional            │
│ EntityManager Proxy       │
│                           │
│ ❌ No Spring Data yet     │
└─────────────┬─────────────┘
              │
              │ Repository
              │ boilerplate removed
              ▼
┌───────────────────────────┐
│ SPRING DATA JPA            │
│                           │
│ JpaRepository             │
│ Derived Queries            │
│ CRUD methods               │
└─────────────┬─────────────┘
              │
              │ Configuration
              │ automation
              ▼
┌───────────────────────────┐
│ SPRING BOOT                │
│                           │
│ Auto Configuration         │
│ DataSource                 │
│ JPA/Hibernate              │
│ Transaction infrastructure │
│ Spring Data                │
│ MVC / Tomcat               │
└───────────────────────────┘
```

---

# 🎯 1A.16 The Key Difference Between Every Stage

| Stage | Who manages the infrastructure? | Repository |
|---|---|---|
| Java + JDBC | **You** | Manual JDBC |
| JPA + Hibernate | **You** | Manual `EntityManager` |
| Spring + JPA + Hibernate | **Spring** | **You write it** |
| Spring Data JPA | **Spring** | Spring generates it |
| Spring Boot | **Spring Boot auto-configures Spring** | Spring Data |

---

# 🧠 1A.17 The Most Important Mental Model

Don't think:

```text
Spring Data → Hibernate → Database
```

as if everything appeared together.

Think:

```text
JDBC
  ↓
JPA + Hibernate
  ↓
Spring integrates with JPA/Hibernate
  ↓
Spring Data removes repository boilerplate
  ↓
Spring Boot automates configuration
```

Each layer solves a **different problem**.

---

# 🔄 1A.18 What Spring Actually Removes

### Before Spring

You:

```text
create EntityManagerFactory
create EntityManager
begin transaction
perform JPA operations
commit / rollback
close EntityManager
```

### Spring

You:

```java
@Transactional
public void createStudent(Student student) {

    entityManager.persist(student);

}
```

Spring handles the surrounding infrastructure.

Conceptually:

```text
Your method
    ↓
Spring Proxy
    ↓
begin transaction
    ↓
Your method
    ↓
flush
    ↓
commit / rollback
    ↓
resource cleanup
```

---

# 🚨 1A.19 What Spring Does NOT Replace

Spring does NOT replace:

```text
JPA
Hibernate
Persistence Context
Entity lifecycle
Dirty checking
Flush
ORM mapping
```

Those concepts still exist.

Spring simply provides the infrastructure around them.

---

# 🔄 1A.20 Transition Into Spring + JPA + Hibernate

We now know how JPA + Hibernate works without Spring.

The architecture was:

```text
Java Code
   ↓
EntityManager
   ↓
EntityTransaction
   ↓
Hibernate
   ↓
JDBC
   ↓
Database
```

Now Spring enters.

Spring doesn't become another ORM.

Instead:

```text
                    SPRING
                       │
          ┌────────────┼────────────┐
          ▼            ▼            ▼
     Bean Management  AOP      Transactions
          │            │            │
          └────────────┼────────────┘
                       ▼
                    JPA
                       ↓
                  Hibernate
                       ↓
                     JDBC
                       ↓
                   Database
```

### Before Spring

YOU controlled:

```text
EntityManagerFactory
EntityManager
EntityTransaction
begin()
commit()
rollback()
close()
```

### After Spring

Spring controls/coordinates:

```text
EntityManagerFactory
EntityManager lifecycle/access
Transaction lifecycle
DataSource
Transaction synchronization
```

And your code becomes:

```java
@Transactional
public void createStudent(Student student) {

    entityManager.persist(student);

}
```

So the big transition is:

```text
MANUAL INFRASTRUCTURE
        ↓
SPRING-MANAGED INFRASTRUCTURE
```

But:

```text
JPA + Hibernate
```

still do the actual ORM work.

# 🧩 2. Entity — What Exactly Is It?

An entity is still just a **normal Java class**.

```java
@Entity
public class Student {

    @Id
    private Long id;

    private String name;

    private int age;
}
```

The annotation tells JPA/Hibernate:

> "Instances of this class participate in persistence and this class maps to relational data."

Conceptually:

```text
Student class
      ↓
student table

Student object
      ↓
potential representation of
a student row
```

But:

> **Java object ≠ database row.**

The object lives in the JVM heap.

The row lives in the database.

Hibernate manages the relationship between them.

---

# 🗺️ 3. The Architecture We Are Building

This is the hierarchy to keep in your head:

```text
HTTP
 │
 ▼
Tomcat
 │
 ▼
Servlet Filters
 │
 ▼
DispatcherServlet
 │
 ▼
Controller
 │
 ▼
Spring Service Proxy
 │
 ▼
Transaction Interceptor
 │
 ▼
Transaction Manager
 │
 ▼
Service Method
 │
 ▼
EntityManager
 │
 ▼
Hibernate
 │
 ▼
Persistence Context
 │
 ├── entity already managed?
 │       │
 │       ├── YES → return existing object
 │       │
 │       └── NO  → database required
 │
 ▼
Hibernate Connection Infrastructure
 │
 ▼
DataSource
 │
 ▼
HikariCP
 │
 ▼
JDBC Connection
 │
 ▼
Database
```

But this is **not one simple linear pipeline**.

There are two important parallel responsibilities:

```text
             Transaction Management
                     │
                     ▼
               Transaction Context
                     │
        ┌────────────┴────────────┐
        │                         │
        ▼                         ▼
 EntityManager               JDBC resources
        │                         │
        ▼                         ▼
    Hibernate                 DataSource
        │                         │
        ▼                         ▼
Persistence Context        JDBC Connection
```

This distinction is the key to understanding everything that follows.

---

# 🧠 4. Spring Beans vs EntityManager vs Persistence Context

This was one of the most important distinctions we established.

## Spring Container

Spring manages your application beans.

For example:

```java
@Service
public class StudentService { }
```

and:

```java
@Repository
public class StudentRepository { }
```

By default, these are **singleton-scoped Spring beans**.

Conceptually:

```text
Spring Container
      │
      ├── StudentController
      ├── StudentService
      ├── StudentRepository
      ├── EntityManagerFactory
      ├── DataSource
      └── TransactionManager
```

The actual Java objects still live in the JVM heap.

Spring Container mainly manages their lifecycle/references.

---

# 🔥 5. Repository Is Usually Singleton

This is important because it can initially look dangerous.

You might have:

```java
@Repository
class StudentRepository {

    public Student find(Long id) {
        ...
    }
}
```

Spring normally creates **one repository bean**.

```text
Spring Container
      │
      ▼
StudentRepository
      │
      └── singleton
```

But that does **not** mean:

> one EntityManager / Persistence Context for the whole application.

That would be disastrous.

Instead, Spring/JPA uses transaction-bound persistence resources.

---

# 🧠 6. EntityManager Is NOT "One Global EntityManager"

This is where the earlier confusion usually happens.

You may write:

```java
@PersistenceContext
private EntityManager entityManager;
```

and it looks like your service has one permanent EntityManager.

But in Spring, the injected reference is typically a **proxy**.

Conceptually:

```text
StudentService singleton
        │
        ▼
EntityManager Proxy
```

The proxy can locate/use the EntityManager associated with the **current transactional context**.

So your singleton service can safely use:

```java
entityManager.find(...)
```

without holding one global persistence context.

---

# 🔥 7. The Actual EntityManager Is Transaction-Bound

Think:

```text
Application
 │
 ├── singleton StudentService
 │
 ├── singleton Repository
 │
 └── EntityManager Proxy
```

Then request 1:

```text
Transaction T1
     │
     ▼
Actual EntityManager EM1
     │
     ▼
Persistence Context PC1
```

Request 2:

```text
Transaction T2
     │
     ▼
Actual EntityManager EM2
     │
     ▼
Persistence Context PC2
```

So:

```text
                Spring singleton
                Service / Repo
                     │
                     ▼
             EntityManager Proxy
                /           \
               /             \
             T1               T2
             │                │
            EM1              EM2
             │                │
            PC1              PC2
```

The **proxy is reusable**.

The actual persistence context is **transaction-scoped in the usual Spring JPA setup**.

---

# 🧠 8. Why Do We Need a Persistence Context?

This is the big Hibernate idea.

Suppose:

```java
Student s1 =
    entityManager.find(Student.class, 1L);

Student s2 =
    entityManager.find(Student.class, 1L);
```

Without a persistence context, Hibernate could repeatedly load objects.

With the persistence context:

```text
find(Student#1)
      ↓
Persistence Context
      ↓
already managed?
      │
      └── YES
           ↓
return same managed object
```

This gives **first-level cache / identity management**.

Conceptually:

```text
Persistence Context
────────────────────────
(Student.class, 1)
       ↓
Student@ABC123
────────────────────────
```

---

# 🧠 9. Persistence Context ≠ JVM Heap

This distinction is crucial.

The Student object lives here:

```text
JVM HEAP
────────────────────
Student@ABC123
id = 1
name = "Sagar"
────────────────────
```

The Persistence Context tracks/manages that object:

```text
Persistence Context
        │
        └──────→ Student@ABC123
```

It doesn't mean the object physically "lives inside" the Persistence Context.

Think:

> **Object = heap object.**
> **Persistence Context = management/tracking context.**

---

# 🔄 10. Entity Lifecycle

An entity moves through states.

```text
new Student()
     │
     ▼
🟡 TRANSIENT
     │
  persist()
     │
     ▼
🟢 MANAGED
     │
     ├───────────────┐
     │               │
 detach()         remove()
     │               │
     ▼               ▼
🔵 DETACHED      🔴 REMOVED
     │
   merge()
     │
     ▼
🟢 MANAGED
```

---

## 🟡 Transient

```java
Student s = new Student();
```

Normal Java object.

Hibernate isn't managing it.

---

## 🟢 Managed

```java
entityManager.persist(s);
```

or:

```java
entityManager.find(Student.class, id);
```

The object becomes associated with the Persistence Context.

Now Hibernate can track its state.

---

## 🔵 Detached

The Java object still exists.

But:

```text
Student object
     X
Persistence Context
```

Hibernate isn't managing that particular object anymore.

---

## 🔴 Removed

```java
entityManager.remove(student);
```

The entity is marked for removal.

The actual `DELETE` is synchronized with the database during flushing.

---

# 🔥 11. `find()` — Full Internal Mental Model

Suppose:

```java
Student s =
    entityManager.find(Student.class, 10L);
```

Think:

```text
EntityManager
     │
     ▼
Hibernate
     │
     ▼
Persistence Context
     │
     ▼
Is Student#10 already managed?
       │
   ┌───┴────┐
  YES       NO
   │         │
   │         ▼
   │     Need database
   │         │
   │         ▼
   │      DataSource
   │         │
   │      HikariCP
   │         │
   │     JDBC Connection
   │         │
   │       SELECT
   │         │
   │      Database
   │         │
   │      ResultSet
   │         │
   │      Hibernate
   │         │
   │      Student object
   │         │
   │         ▼
   │   Persistence Context
   │         │
   └────┬────┘
        ▼
   Managed Student
        │
        ▼
   return to caller
```

### Critical point

If the entity is already managed:

```text
NO database query
NO connection necessarily required
```

The operation can be satisfied entirely from the Persistence Context.

---

# 🧠 12. Hibernate Session and Persistence Context

Hibernate's native API is:

```java
Session
```

JPA's standard API is:

```java
EntityManager
```

Don't think:

```text
Session extends EntityManager
```

as the mental model.

Instead:

```text
JPA
 │
 └── EntityManager contract

Hibernate
 │
 └── Session native API
```

Hibernate integrates its Session/persistence machinery with the JPA EntityManager contract.

Conceptually:

```text
JPA EntityManager
       │
       ▼
Hibernate
       │
       ▼
Hibernate Session
       │
       ▼
Persistence Context / managed entity state
```

The important point for now:

> **EntityManager is the JPA API your application uses; Session is Hibernate's native persistence API.**

---

# 🔥 13. Now Add `@Transactional`

This is where Spring AOP knowledge becomes useful.

You write:

```java
@Transactional
public void updateStudent(Long id) {

    Student s =
        entityManager.find(Student.class, id);

    s.setName("Sagar");
}
```

Spring sees `@Transactional`.

Conceptually:

```text
StudentService TARGET
        ▲
        │
Transactional Proxy
```

When you call:

```java
studentService.updateStudent(10L);
```

you are usually calling the proxy:

```text
Caller
  │
  ▼
Spring Proxy
  │
  ▼
TransactionInterceptor
  │
  ▼
TransactionManager
  │
  ▼
Actual Service Method
```

This should feel almost identical to your AOP mental model.

---

# 🧩 14. TransactionInterceptor

Conceptually:

```java
Object invoke(...) {

    TransactionStatus status =
        transactionManager.begin(...);

    try {

        Object result =
            targetMethod.invoke(...);

        transactionManager.commit(status);

        return result;

    } catch (Exception e) {

        transactionManager.rollback(status);

        throw e;
    }
}
```

This is **conceptual pseudocode**, not the actual Spring source code.

But it captures the architecture.

---

# 🏛️ 15. TransactionManager

Spring has a transaction abstraction.

Conceptually:

```java
interface PlatformTransactionManager {

    TransactionStatus getTransaction(...);

    void commit(...);

    void rollback(...);
}
```

Different technologies can have different implementations.

Conceptually:

```text
PlatformTransactionManager
          ▲
          │
     ┌────┴─────┐
     │          │
     ▼          ▼
JDBC Manager   JpaTransactionManager
     │          │
     ▼          ▼
   JDBC       JPA/Hibernate
```

The TransactionManager is responsible for **transaction coordination**, not entity management.

---

# 🧠 16. Transaction ≠ JDBC Connection

This was the hardest concept, so lock it down.

Your old JDBC mental model was:

```text
Connection
    ↓
setAutoCommit(false)
    ↓
SQL
    ↓
commit / rollback
```

That is correct at the low level.

Spring gives you a higher abstraction:

```text
@Transactional
       ↓
Spring Proxy
       ↓
TransactionInterceptor
       ↓
TransactionManager
       ↓
transaction context
```

The physical JDBC connection may be acquired when Hibernate actually needs DB access.

So:

```text
Logical Transaction
──────────────────────────────────
       │
       │ connection may not exist yet
       │
       ├── Persistence Context check
       │
       └── DB required
              ↓
          DataSource
              ↓
          Connection
              ↓
        physical DB work
──────────────────────────────────
```

Therefore:

> **A transaction is not simply a Connection object.**

A Connection is one of the resources that can participate in the transaction.

---

# 🧵 17. TransactionSynchronizationManager

Spring needs a way to associate transaction-related resources with the current execution context.

Conceptually:

```text
Current Thread
──────────────────────────
Transaction context
EntityManager/resource
Transaction synchronizations
JDBC resources when applicable
──────────────────────────
```

This is where:

```text
TransactionSynchronizationManager
```

comes in.

Think of it as Spring's **transaction-resource bookkeeping/binding mechanism**.

It allows transaction-aware components to find resources associated with the current transaction instead of manually passing:

```java
Connection connection
```

through every service/repository method.

---

# 🔥 18. The Transaction + EntityManager Relationship

This is the important flow:

```text
@Transactional
      │
      ▼
Spring Proxy
      │
      ▼
TransactionInterceptor
      │
      ▼
JpaTransactionManager
      │
      ▼
JPA transaction/context coordination
      │
      ▼
EntityManager associated with transaction
      │
      ▼
Hibernate
      │
      ▼
Persistence Context
```

So the EntityManager isn't some global persistence object shared by every request.

Instead:

```text
Transaction T1 → EM1 → PC1

Transaction T2 → EM2 → PC2

Transaction T3 → EM3 → PC3
```

while the Spring repository/service beans can remain singleton.

---

# 🔌 19. Where DataSource Fits

The DataSource is a separate responsibility.

```text
DataSource
    ↓
HikariCP
    ↓
Connection Pool
```

Hibernate's connection infrastructure can obtain a connection:

```text
Hibernate
   ↓
Connection infrastructure
   ↓
DataSource
   ↓
HikariCP
   ↓
JDBC Connection
```

Then:

```text
JDBC Connection
      ↓
PreparedStatement
      ↓
SQL
      ↓
Database
```

---

# 🧠 20. The Crucial Connection Question

You asked:

> "How can TransactionManager start a transaction if the connection doesn't exist yet?"

Because these are different layers.

### Before connection:

```text
Spring
 ↓
TransactionManager
 ↓
transaction context established
```

### When Hibernate needs DB:

```text
Hibernate
 ↓
DataSource
 ↓
Hikari
 ↓
JDBC Connection
```

The connection then participates in the already-established transaction/resource context.

So don't visualize:

```text
TransactionManager
      ↓
creates connection
      ↓
gives connection to Hibernate
```

as your primary model.

Visualize:

```text
                Transaction
                     │
          ┌──────────┴──────────┐
          │                     │
          ▼                     ▼
     EntityManager          DB resources
          │                     │
          ▼                     ▼
      Hibernate             DataSource
          │                     │
          ▼                   Hikari
 Persistence Context            │
                              JDBC
                             Connection
```

---

# 🚀 21. Complete Request Flow

Now let's execute a real request.

Controller:

```java
@PutMapping("/students/{id}")
public Student update(@PathVariable Long id) {

    return studentService.update(id);
}
```

Service:

```java
@Transactional
public Student update(Long id) {

    Student s =
        entityManager.find(Student.class, id);

    s.setName("Sagar");

    return s;
}
```

---

## Phase 1 — Request

```text
Client
  ↓
HTTP
  ↓
Tomcat
  ↓
Filters
  ↓
DispatcherServlet
  ↓
Controller
```

---

## Phase 2 — Controller calls Service

```text
Controller
    ↓
StudentService Proxy
```

Not necessarily the raw target object.

---

## Phase 3 — Transaction interception

```text
Proxy
  ↓
TransactionInterceptor
  ↓
JpaTransactionManager
```

Transaction context is established.

---

## Phase 4 — Actual Service Method

```text
TransactionInterceptor
       ↓
actual StudentService.update()
```

Now:

```java
entityManager.find(...)
```

---

## Phase 5 — EntityManager

```text
EntityManager Proxy
       ↓
transaction-associated EntityManager
       ↓
Hibernate
```

---

## Phase 6 — Persistence Context

Hibernate asks:

```text
Is Student#10 already managed?
```

### YES

```text
Persistence Context
       ↓
Student#10
       ↓
return same object
```

No database query required.

### NO

```text
Persistence Context
       ↓
not found
       ↓
Hibernate needs DB
```

---

# 🔌 22. Database Branch

Hibernate obtains a connection through its connection infrastructure:

```text
Hibernate
   ↓
DataSource
   ↓
HikariCP
   ↓
JDBC Connection
   ↓
PreparedStatement
   ↓
SELECT
   ↓
Database
```

Result:

```text
Database
   ↓
ResultSet
   ↓
Hibernate
   ↓
Student object
   ↓
Persistence Context
   ↓
MANAGED
```

Now your local variable points to that managed object:

```text
JVM HEAP
────────────────────────
Student@ABC
id = 10
name = "Old Name"
────────────────────────
       ▲
       │
       ├── service variable
       │
       └── Persistence Context
```

---

# ✏️ 23. Business Logic Modifies Object

```java
s.setName("Sagar");
```

No SQL necessarily happens here.

```text
Java object
     ↓
field changed
     ↓
Persistence Context still manages it
```

---

# 🚿 24. Flush + Dirty Checking

At synchronization/flush:

```text
Persistence Context
       ↓
Hibernate Dirty Checking
```

Hibernate conceptually compares:

```text
Original snapshot:
name = "Old Name"

Current object:
name = "Sagar"
```

Different:

```text
OLD ≠ CURRENT
      ↓
Generate UPDATE
```

```sql
UPDATE student
SET name = ?
WHERE id = ?
```

Then:

```text
Hibernate
   ↓
JDBC
   ↓
Connection
   ↓
Database
```

---

# 💥 25. Flush Does NOT Mean Commit

This deserves its own box:

```text
FLUSH
────────────────────
Persistence Context
       ↓
Dirty Checking
       ↓
SQL
       ↓
Database
────────────────────

Transaction still ACTIVE
```

Whereas:

```text
COMMIT
────────────────────
Transaction successfully ends
────────────────────
```

Therefore:

```text
flush()
   ↓
UPDATE SQL
   ↓
RuntimeException
   ↓
ROLLBACK
```

The update can still be undone.

---

# 🏁 26. Successful Completion

Service method returns:

```text
Service Method
      ↓
TransactionInterceptor
```

Transaction completes successfully.

Conceptually:

```text
JpaTransactionManager
       ↓
flush/synchronization
       ↓
commit
       ↓
transaction complete
```

The JDBC connection is released/returned to the pool.

```text
JDBC Connection
      ↓
HikariCP
      ↓
available again
```

---

# 💥 27. Exception Completion

Suppose:

```java
@Transactional
public void update() {

    Student s = entityManager.find(...);

    s.setName("Sagar");

    throw new RuntimeException();
}
```

Flow:

```text
Service
  ↓
RuntimeException
  ↓
TransactionInterceptor
  ↓
JpaTransactionManager
  ↓
ROLLBACK
```

Default Spring behavior:

```text
RuntimeException → rollback
Error            → rollback
checked Exception → normally no automatic rollback
```

Unless configured, for example:

```java
@Transactional(rollbackFor = Exception.class)
```

---

# 🔄 28. Response Returns

After successful service completion:

```text
Service
   ↓
Controller
   ↓
Jackson / Message Converter
   ↓
JSON
   ↓
DispatcherServlet
   ↓
Filters
   ↓
Tomcat
   ↓
Client
```

So the entire request has travelled:

```text
HTTP
 ↓
Controller
 ↓
Proxy
 ↓
Transaction
 ↓
Service
 ↓
EntityManager
 ↓
Hibernate
 ↓
Persistence Context
 ↓
DataSource/JDBC when needed
 ↓
Database
 ↓
Hibernate
 ↓
Transaction completion
 ↓
Controller
 ↓
JSON
 ↓
HTTP
```

---

# 🧠 29. Why Business Logic Inside `@Transactional` Is Not "Using the Connection"

Suppose:

```java
@Transactional
public void placeOrder() {

    validate();

    calculatePrice();

    repository.save(order);

    sendEmail();

    inventoryRepository.reduceStock();
}
```

The transaction defines a **scope**:

```text
TRANSACTION SCOPE
────────────────────────────────
validate()          → normal Java

calculatePrice()    → normal Java

repository.save()   → DB-aware

sendEmail()         → external system

inventory update    → DB-aware
────────────────────────────────
          ↓
    COMMIT / ROLLBACK
```

The transaction doesn't magically make:

```text
Java method
Email server
HTTP server
```

transactional.

Only transaction-aware resources participate.

---

# ⚠️ 30. `@Async` Is Different

If you call an async method:

```java
@Transactional
public void process() {

    repository.save(...);

    notificationService.sendAsync();
}
```

and:

```java
@Async
public void sendAsync() {
}
```

the async operation executes on another thread.

The original transaction context does not simply follow it automatically.

So:

```text
Main Thread
────────────────────
Transaction T1
    │
    └── @Async
           │
           ▼
      Other Thread
           │
           └── not automatically T1
```

This becomes important later when learning distributed systems and async processing.

---

# 🧩 31. Propagation — Core Mental Model

You don't need the full transaction deep dive yet.

Just remember:

## `REQUIRED` — default

```text
Transaction already exists?
       │
   ┌───┴────┐
  YES       NO
   │         │
 JOIN      CREATE
```

So:

```java
@Transactional
```

means:

```java
@Transactional(propagation = Propagation.REQUIRED)
```

by default.

---

## `REQUIRES_NEW`

```text
Outer Transaction T1
        ↓
     SUSPEND
        ↓
New Transaction T2
        ↓
T2 completes
        ↓
Resume T1
```

T1 and T2 are separate transactions.

---

## `NESTED`

Conceptually:

```text
Transaction T1
      ↓
 SAVEPOINT
      ↓
nested work
      ↓
rollback to savepoint
```

Full propagation mechanics belong in the later transaction deep dive.

---

# ⚠️ 32. Self Invocation

This is directly connected to your AOP understanding.

```java
public void outer() {
    inner();
}

@Transactional
public void inner() {
}
```

If:

```java
this.inner();
```

then:

```text
Proxy
  ↓
outer()
  ↓
this.inner()
```

The call doesn't go back through the proxy.

Therefore:

```text
@Transactional on inner()
       ↓
proxy interception doesn't happen
```

But:

```text
Another Bean
      ↓
Spring Proxy
      ↓
inner()
```

does pass through the proxy.

---

# 🧠 33. Repository Singleton vs Persistence Context

This is worth explicitly remembering:

```text
Spring Container
─────────────────────────────

StudentRepository
      │
      └── singleton

StudentService
      │
      └── singleton

EntityManagerFactory
      │
      └── long-lived factory

DataSource
      │
      └── long-lived pool

TransactionManager
      │
      └── Spring infrastructure
```

But per transaction:

```text
Transaction T1
    │
    ├── EntityManager
    └── Persistence Context

Transaction T2
    │
    ├── EntityManager
    └── Persistence Context
```

So:

> **Singleton Spring beans can safely participate in many transactions because the transaction-bound persistence resources are not the same thing as the singleton bean.**

---

# 🧠 34. The Four Different Lifetimes

This distinction is extremely useful.

### Application lifetime

```text
Spring Container
    ↓
Service / Repository / DataSource / EMF
```

Long-lived.

### Transaction lifetime

```text
Transaction
    ↓
transaction-associated EntityManager
    ↓
Persistence Context
```

Usually short-lived.

### Connection lifetime

```text
JDBC Connection
    ↓
borrowed from Hikari
    ↓
used
    ↓
returned to pool
```

Managed separately.

### Java object lifetime

```text
Entity object
    ↓
JVM heap
    ↓
GC when unreachable
```

Independent of the above.

Therefore:

```text
Context ends
     ≠
Java object immediately destroyed

Connection returned to pool
     ≠
physical DB connection necessarily destroyed

Repository singleton
     ≠
one global Persistence Context
```

---

# 🔥 35. The Complete Mental Model

This is the diagram to keep as your master reference:

```text
                         🌐 HTTP REQUEST
                                │
                                ▼
                            TOMCAT
                                │
                                ▼
                             FILTERS
                                │
                                ▼
                       DispatcherServlet
                                │
                                ▼
                           CONTROLLER
                                │
                                │ calls service
                                ▼
                    ┌──────────────────────┐
                    │ Spring Proxy         │
                    │ @Transactional       │
                    └──────────┬───────────┘
                               │
                               ▼
                    TransactionInterceptor
                               │
                               ▼
                    JpaTransactionManager
                               │
                    ┌──────────┴──────────┐
                    │                     │
                    ▼                     ▼
             Transaction Context    Synchronization/
                                    Resource Binding
                                         │
                                         ▼
                              TransactionSynchronizationManager
                                         │
                                         ▼
                              current transaction resources
                                         │
                               ┌─────────┴─────────┐
                               │                   │
                               ▼                   ▼
                        EntityManager          DB Resources
                         (proxy/actual)             │
                               │                    │
                               ▼                    ▼
                           Hibernate             DataSource
                               │                    │
                               ▼                  HikariCP
                     ┌─────────────────┐            │
                     │ Hibernate       │            ▼
                     │ Session         │       JDBC Connection
                     │                 │            │
                     │ Persistence     │            │
                     │ Context        │            │
                     │                 │            │
                     │ Entity Map      │            │
                     │ Snapshots       │            │
                     └────────┬────────┘            │
                              │                     │
                              ▼                     │
                  Is Entity Already Managed?       │
                         /             \            │
                       YES             NO           │
                        │               │            │
                        │               ▼            │
                        │          Hibernate needs  │
                        │             database      │
                        │               │            │
                        │               └────────────┤
                        │                            ▼
                        │                      JDBC Connection
                        │                            │
                        │                           SQL
                        │                            │
                        │                            ▼
                        │                        DATABASE
                        │                            │
                        │                         ResultSet
                        │                            │
                        │                            ▼
                        │                        Hibernate
                        │                            │
                        └──────────────┬─────────────┘
                                       ▼
                              MANAGED ENTITY
                                       │
                                       ▼
                              JVM HEAP OBJECT
                                       │
                                       ▼
                              Business Logic
                                       │
                               setName("Sagar")
                                       │
                                       ▼
                              Persistence Context
                                       │
                                       ▼
                              FLUSH / DIRTY CHECK
                                       │
                         compare current state
                         with original snapshot
                                       │
                               changes found?
                                       │
                                       ▼
                                  UPDATE SQL
                                       │
                                       ▼
                                JDBC Connection
                                       │
                                       ▼
                                   DATABASE
                                       │
                                       ▼
                            SUCCESS / EXCEPTION
                              /             \
                           SUCCESS         FAILURE
                              │               │
                              ▼               ▼
                            COMMIT         ROLLBACK
                              │               │
                              └───────┬───────┘
                                      ▼
                              Transaction Ends
                                      │
                                      ▼
                            JDBC resource released
                                      │
                                      ▼
                                  Hikari Pool
                                      │
                                      ▼
                                  Controller
                                      │
                                      ▼
                                  Jackson
                                      │
                                      ▼
                                JSON Response
                                      │
                                      ▼
                                  🌐 CLIENT
```

---

# 🎯 36. The "Who Manages What?" Cheat Sheet

| Component                             | Main responsibility                                             |
| ------------------------------------- | --------------------------------------------------------------- |
| **Spring Container**                  | Creates/manages Spring beans                                    |
| **Controller**                        | Handles HTTP request/response                                   |
| **Service**                           | Business logic                                                  |
| **Repository**                        | Data-access abstraction                                         |
| **Transactional Proxy**               | Intercepts transactional method calls                           |
| **TransactionInterceptor**            | Begins/completes transaction around method                      |
| **TransactionManager**                | Coordinates transaction lifecycle/resources                     |
| **TransactionSynchronizationManager** | Associates transaction resources/context with current execution |
| **EntityManager Proxy**               | Provides access to transaction-associated EntityManager         |
| **EntityManager**                     | JPA persistence API                                             |
| **Hibernate**                         | JPA implementation + ORM machinery                              |
| **Session**                           | Hibernate's native persistence API                              |
| **Persistence Context**               | Tracks managed entities/state                                   |
| **Dirty Checking**                    | Detects changes in managed entities                             |
| **Flush**                             | Synchronizes entity state → SQL/DB                              |
| **DataSource**                        | Provides JDBC connections                                       |
| **HikariCP**                          | Pools/reuses connections                                        |
| **JDBC Connection**                   | Low-level DB connection/transaction resource                    |
| **Database**                          | Executes SQL and enforces DB transaction semantics              |

---

# 🧭 37. The Mental Model You Should Now Carry

Don't memorize Hibernate as:

```text
100 APIs
100 annotations
100 classes
```

Instead think in **five layers**:

```text
┌──────────────────────────────┐
│ 1. SPRING                     │
│ Proxy + Transaction Manager   │
└──────────────┬───────────────┘
               ↓
┌──────────────────────────────┐
│ 2. JPA                        │
│ EntityManager + contract      │
└──────────────┬───────────────┘
               ↓
┌──────────────────────────────┐
│ 3. HIBERNATE                  │
│ Session + Persistence Context │
│ Dirty Checking + Flush       │
└──────────────┬───────────────┘
               ↓
┌──────────────────────────────┐
│ 4. JDBC                       │
│ DataSource + Connection       │
└──────────────┬───────────────┘
               ↓
┌──────────────────────────────┐
│ 5. DATABASE                   │
│ SQL + Transactions            │
└──────────────────────────────┘
```

But remember that **transactions cut across these layers**:

```text
                 @Transactional
                       │
                       ▼
                Transaction
                       │
          ┌────────────┴────────────┐
          │                         │
          ▼                         ▼
       JPA/Hibernate            JDBC resources
          │                         │
          ▼                         ▼
 Persistence Context          Connection
```

---

# 🚀 38. What We Deep-Dive Next

Now that the map is solid, go **one box at a time**.

### Phase 1 — Entity Lifecycle

```text
Transient
Managed
Detached
Removed
persist()
find()
merge()
remove()
detach()
clear()
```

### Phase 2 — Persistence Context Internals

```text
Entity identity
First-level cache
Entity map
Snapshots
Managed state
Context lifetime
```

### Phase 3 — Dirty Checking + Flush

```text
Snapshots
 ↓
Dirty checking
 ↓
Flush
 ↓
SQL generation
 ↓
Flush modes
 ↓
Query-triggered auto-flush
```

### Phase 4 — Hibernate Internals

```text
EntityManager
 ↓
Hibernate Session
 ↓
SessionImpl
 ↓
Persistence Context
 ↓
Action Queue
 ↓
Flush
 ↓
SQL generation
```

### Phase 5 — Transactions

```text
@Transactional
 ↓
Proxy
 ↓
TransactionInterceptor
 ↓
PlatformTransactionManager
 ↓
JpaTransactionManager
 ↓
TransactionSynchronizationManager
 ↓
EntityManager / JDBC resources
 ↓
Commit / Rollback
```

### Phase 6 — Querying

```text
JPQL
HQL
Native SQL
Parameters
Query execution
Persistence Context interaction
Auto-flush before queries
```

### Phase 7 — Relationships

```text
@OneToOne
@OneToMany
@ManyToOne
@ManyToMany
@JoinColumn
mappedBy
Cascade
Orphan removal
```

### Phase 8 — Performance

```text
Lazy loading
N+1
Fetch Join
Entity Graph
Batch fetching
L1 Cache
L2 Cache
```

### Phase 9 — Concurrency

```text
Optimistic locking
@Version
Pessimistic locking
Isolation
Lost updates
```

---

# 🏁 The One Mental Picture

If you can explain this without looking at the README, you've understood the architecture:

```text
Client
  ↓
Controller
  ↓
Spring Transactional Proxy
  ↓
TransactionInterceptor
  ↓
TransactionManager
  ↓
Service
  ↓
EntityManager Proxy
  ↓
transaction-associated EntityManager
  ↓
Hibernate Session
  ↓
Persistence Context
  │
  ├── entity exists?
  │       │
  │       ├── YES → existing managed Java object
  │       │
  │       └── NO → Hibernate → DataSource → Hikari → JDBC → DB
  │
  ↓
Managed Entity
  ↓
Java modification
  ↓
Dirty Checking
  ↓
Flush
  ↓
SQL
  ↓
JDBC
  ↓
Database
  ↓
Commit / Rollback
  ↓
Transaction ends
  ↓
Controller
  ↓
JSON
  ↓
Client
```

### 🔥 The deepest idea

> **Spring manages the application-level transaction boundary. JPA provides the persistence contract. Hibernate manages the entity state and object-relational synchronization. The Persistence Context manages the entities for that transactional unit of work. DataSource/Hikari provides the JDBC resources when database access is needed. JDBC and the database ultimately perform the physical database work.**

And the most important separation:

```text
Repository/Service bean
       ↓
usually singleton

EntityManager
       ↓
transaction-associated access/proxy

Persistence Context
       ↓
typically transaction-scoped

Entity object
       ↓
JVM heap

JDBC Connection
       ↓
pooled DB resource

Transaction
       ↓
logical unit of database work
```

**That is the foundation.** From here, every Hibernate feature we learn should attach to one of these boxes rather than becoming another random piece of magic.
