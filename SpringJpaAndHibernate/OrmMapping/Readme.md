# 🚀 JPA & Hibernate — Part 1

## Introduction + Complete JPA Mapping

> 🎯 **Goal:** Understand why ORM exists, what JPA provides, and how Java objects are mapped to relational data.
>
> 🧠 **Not covered yet:** Persistence Context, Entity Lifecycle, Dirty Checking, Flush, Proxies, Lazy Loading internals, Hibernate Session internals, First-Level Cache, etc.
>
> Those belong to the **Hibernate Internals** phase.

---

# 🗺️ 1. Where We Came From

```text
                    DATABASE
                       ▲
                       │
                     JDBC
                       │
                       ▼
                Java Application
```

With JDBC, we manually handled the translation between Java and SQL.

Then came:

```text
JDBC
  ↓
Spring JdbcTemplate
  ↓
ORM
  ↓
JPA
  ↓
Hibernate
```

Each layer solves a different problem.

---

# 🧱 2. The Problem With JDBC

Java works with:

```text
Objects
Classes
Fields
Collections
Inheritance
Enums
Value Objects
```

A relational database works with:

```text
Tables
Rows
Columns
Primary Keys
Foreign Keys
Relationships
```

So we have two different models:

```text
       OBJECT MODEL
            │
            │  mismatch
            ▼
     RELATIONAL MODEL
```

This is called the **Object-Relational Impedance Mismatch**.

---

# 🔧 3. What JDBC Required

With JDBC we had to repeatedly perform things like:

```text
SQL
 ↓
PreparedStatement
 ↓
set parameters
 ↓
execute
 ↓
ResultSet
 ↓
read columns
 ↓
construct Java object
```

Example:

```java
PreparedStatement ps =
    connection.prepareStatement(
        "SELECT id, name, age FROM student WHERE id = ?"
    );

ps.setLong(1, id);

ResultSet rs = ps.executeQuery();

Student student = new Student();

if (rs.next()) {
    student.setId(rs.getLong("id"));
    student.setName(rs.getString("name"));
    student.setAge(rs.getInt("age"));
}
```

---

# ⚡ 4. What Spring JdbcTemplate Solved

`JdbcTemplate` removed much of the repetitive JDBC plumbing.

It handles things such as:

* 🔌 Connection management
* 📝 Statement creation
* 🎯 Parameter binding infrastructure
* 🧹 Resource cleanup
* ⚠️ Exception translation
* 🔄 ResultSet processing infrastructure

But we still had to describe:

```text
Database
   ↓
ResultSet
   ↓
Java object
```

For example:

```java
student.setId(rs.getLong("id"));
student.setName(rs.getString("name"));
student.setAge(rs.getInt("age"));
```

So:

> **JdbcTemplate automated JDBC mechanics, but it didn't fundamentally model our Java objects as relational data.**

---

# 🚀 5. What ORM Solves

ORM = **Object-Relational Mapping**

The idea is simple:

> 🧠 Describe how Java objects correspond to relational structures, then let the ORM automate repetitive object-relational operations.

Instead of repeatedly writing:

```text
Student.id   → student.id
Student.name → student.name
Student.age  → student.age
```

we describe the mapping:

```java
@Entity
public class Student {

    @Id
    private Long id;

    private String name;

    private int age;
}
```

Hibernate can then understand:

```text
Student
   │
   ├── id   ─────→ id
   ├── name ─────→ name
   └── age  ─────→ age
                    │
                    ▼
                 STUDENT
```

---

# 💡 6. ORM Is More Than "No SQL"

A common beginner understanding is:

> ORM = framework that writes SQL so I don't have to.

That's incomplete.

The deeper idea is:

```text
Java Object Model
       ↕
Mapping Metadata
       ↕
Relational Model
```

Once Hibernate knows the mapping, it can automate:

* 🔄 Object ↔ Row mapping
* 🎯 Parameter binding
* 📥 Result mapping
* 📝 INSERT generation
* ✏️ UPDATE generation
* 🗑️ DELETE generation
* 🔗 Relationship persistence
* 🔄 Type conversions

You **can still write SQL** with Hibernate/JPA when appropriate.

ORM is about **mapping and persistence automation**, not eliminating SQL.

---

# ☕ 7. JPA vs Hibernate

These are not interchangeable terms.

## JPA

JPA is a **Jakarta specification/API** for ORM.

It defines concepts such as:

```java
@Entity
@Id
@Column
@OneToMany
@ManyToOne
@Embedded
@ElementCollection
```

and APIs such as:

```java
EntityManager
```

JPA defines the standard contract.

---

## 🐻 Hibernate

Hibernate is an actual ORM implementation.

Conceptually:

```text
Your Application
       │
       ▼
      JPA
  specification/API
       │
       ▼
   Hibernate
 implementation
       │
       ▼
      JDBC
       │
       ▼
   Database
```

Therefore:

> ☕ **JPA = specification/API**

> 🐻 **Hibernate = implementation**

Hibernate also provides functionality beyond the JPA specification.

---

# 🧭 8. JPA Mapping — The Big Picture

JPA mapping allows us to describe:

```text
Java                         Relational
────────────────────────────────────────────
@Entity              →      Table

@Id                  →      Primary Key

Field                →      Column

@Embedded            →      Columns in same table

@ElementCollection   →      Collection table

@ManyToOne           →      Foreign Key

@OneToMany           →      Related rows

@ManyToMany          →      Join Table

@Enumerated           →      Enum representation

@Convert              →      Custom conversion
```

Now let's go layer by layer.

---

# 🏷️ 9. `@Entity`

```java
@Entity
public class Student {
}
```

Means:

> This class is a persistent entity.

Hibernate can manage instances of this class as persistent objects.

Conceptually:

```text
Student.java
     ↓
   @Entity
     ↓
STUDENT table
```

---

# 🔑 10. `@Id`

Every JPA entity needs an identifier.

```java
@Entity
public class Student {

    @Id
    private Long id;
}
```

This represents the entity's identity.

Conceptually:

```text
Java Entity
     │
     │ identity
     ▼
Database Primary Key
```

Example:

```text
Student object
      │
      └── id = 101
              │
              ▼
        STUDENT.id = 101
```

⚠️ This becomes extremely important later when we study the **Persistence Context**.

---

# 🆔 11. Generated IDs — `@GeneratedValue`

Usually we don't want to manually generate database IDs.

```java
@Id
@GeneratedValue
private Long id;
```

JPA allows the persistence provider to generate the identifier.

Common strategies include:

```java
@GeneratedValue(strategy = GenerationType.IDENTITY)
```

```java
@GeneratedValue(strategy = GenerationType.SEQUENCE)
```

```java
@GeneratedValue(strategy = GenerationType.TABLE)
```

and:

```java
@GeneratedValue(strategy = GenerationType.AUTO)
```

### Important conceptual difference

```text
@Id
   ↓
"What identifies this entity?"

@GeneratedValue
   ↓
"How is that identifier generated?"
```

For example:

```text
Database-generated ID
        ↓
INSERT
        ↓
Database generates ID
        ↓
Hibernate obtains generated ID
```

The exact behavior depends on the generation strategy and database.

---

# 🗄️ 12. `@Table`

Customize the database table name.

```java
@Entity
@Table(name = "students")
public class Student {
}
```

Mapping:

```text
Student
   ↓
students
```

`@Table` can also specify additional table-level mapping information.

---

# 📊 13. `@Column`

Customize a field's column mapping.

```java
@Column(name = "student_name")
private String name;
```

Mapping:

```text
name
  │
  ▼
student_name
```

Other useful attributes include:

```java
@Column(
    name = "student_name",
    nullable = false,
    length = 100,
    unique = true
)
private String name;
```

Conceptually:

```text
name
 │
 ├── column name
 ├── nullability
 ├── length
 └── uniqueness metadata
```

---

# 🧬 14. Default Field Mapping

You don't need `@Column` on every field.

For example:

```java
@Entity
public class Student {

    @Id
    private Long id;

    private String name;

    private int age;
}
```

JPA can derive mappings from the Java class/field names and provider conventions.

So:

```text
name → name
age  → age
```

You use explicit annotations when you need different mapping behavior.

---

# 🔤 15. Enum Mapping — `@Enumerated`

Java has enums:

```java
public enum Status {
    ACTIVE,
    INACTIVE,
    SUSPENDED
}
```

Entity:

```java
@Enumerated(EnumType.STRING)
private Status status;
```

The enum can be stored as:

```text
"ACTIVE"
"INACTIVE"
"SUSPENDED"
```

### Two main choices

```java
EnumType.STRING
```

or

```java
EnumType.ORDINAL
```

---

## ⚠️ `ORDINAL`

```java
@Enumerated(EnumType.ORDINAL)
private Status status;
```

Potential database values:

```text
ACTIVE     → 0
INACTIVE   → 1
SUSPENDED  → 2
```

Problem:

If we later change:

```java
enum Status {
    ACTIVE,
    NEW_STATUS,
    INACTIVE,
    SUSPENDED
}
```

the numeric meanings shift.

💀 Existing data can now represent the wrong enum.

---

## ✅ `STRING`

```java
@Enumerated(EnumType.STRING)
private Status status;
```

Database:

```text
ACTIVE
INACTIVE
SUSPENDED
```

This is generally safer because the database stores the enum name rather than its position.

### Mental model

```text
Enum
 ↓
@Enumerated
 ↓
Database representation
```

---

# 📅 16. Date and Time Mapping

Modern Java date/time types can be mapped naturally.

Example:

```java
private LocalDate birthDate;

private LocalDateTime createdAt;

private Instant updatedAt;
```

Conceptually:

```text
LocalDate
   ↓
DATE

LocalDateTime
   ↓
TIMESTAMP-like representation

Instant
   ↓
timestamp representing an instant
```

For modern `java.time` types, JPA/Hibernate generally knows how to map them without old-style temporal annotations.

---

# ⏰ 17. Legacy Date Mapping — `@Temporal`

Older Java applications may use:

```java
java.util.Date
```

For those types, JPA provides:

```java
@Temporal(TemporalType.DATE)
private Date birthDate;
```

Possible temporal types:

```text
DATE
TIME
TIMESTAMP
```

This is mostly relevant when dealing with legacy `Date`/`Calendar` mappings.

---

# 🔄 18. Custom Attribute Conversion — `@Converter`

Sometimes Java and database representations don't directly match.

Example:

```text
Java
Status.ACTIVE

        ↕ converter

Database
"A"
```

JPA provides:

```java
@Converter
```

Conceptually:

```text
Java Value
    ↕
AttributeConverter
    ↕
Database Value
```

Example:

```java
@Converter
public class StatusConverter
        implements AttributeConverter<Status, String> {

    // Java → DB

    // DB → Java
}
```

This is useful when the database representation is different from the normal JPA representation.

---

# 🧩 19. Composite / Value Objects

Suppose:

```java
class Address {

    private String street;
    private String city;
    private String pincode;
}
```

Address may not need:

```text
address.id
address table
independent lifecycle
```

It may simply be a value belonging to Student.

JPA supports this with:

```java
@Embeddable
```

---

# 📦 20. `@Embeddable`

```java
@Embeddable
public class Address {

    private String street;
    private String city;
    private String pincode;
}
```

Means:

> This class can be embedded into an entity.

It represents a **value object**, not an independent entity.

---

# 📥 21. `@Embedded`

```java
@Entity
public class Student {

    @Id
    private Long id;

    private String name;

    @Embedded
    private Address address;
}
```

The fields of `Address` can become columns in the Student table.

```text
STUDENT
────────────────────
id
name
street
city
pincode
────────────────────
```

Java:

```text
Student
   │
   └── Address
        ├── street
        ├── city
        └── pincode
```

Database:

```text
Student table
   │
   ├── street
   ├── city
   └── pincode
```

---

# 🎨 22. `@AttributeOverride`

Suppose:

```java
@Embedded
private Address homeAddress;

@Embedded
private Address workAddress;
```

Both contain:

```text
city
pincode
```

We need different database columns.

```java
@Embedded
@AttributeOverrides({
    @AttributeOverride(
        name = "city",
        column = @Column(name = "home_city")
    ),
    @AttributeOverride(
        name = "pincode",
        column = @Column(name = "home_pincode")
    )
})
private Address homeAddress;
```

Result:

```text
home_city
home_pincode

work_city
work_pincode
```

💡 **The Java structure stays the same; only the relational mapping changes.**

---

# 📚 23. Collections of Values

Consider:

```java
private List<String> phoneNumbers;
```

A normal column isn't naturally a collection.

We need another relational structure.

```text
STUDENT
────────────────
id
name
────────────────

STUDENT_PHONE
────────────────
student_id
phone_number
────────────────
```

This is where:

```java
@ElementCollection
```

comes in.

---

# 📦 24. `@ElementCollection`

```java
@ElementCollection
private List<String> phoneNumbers;
```

This means:

> This is a collection of value types belonging to the entity.

Conceptually:

```text
Student
   │
   └── phoneNumbers
          │
          ├── 987...
          ├── 876...
          └── 765...
```

Database:

```text
STUDENT
   │
   │ 1
   ▼
STUDENT_PHONE
   │
   ├── phone
   ├── phone
   └── phone
```

The values are **not independent entities**.

---

# 🔗 25. `@CollectionTable`

Customize the collection table:

```java
@ElementCollection
@CollectionTable(
    name = "student_phone",
    joinColumns = @JoinColumn(name = "student_id")
)
private List<String> phoneNumbers;
```

Now we explicitly describe:

```text
Collection Table
       │
       └── student_id
                 │
                 ▼
             STUDENT.id
```

---

# 🧱 26. Element Collection of Embeddables

We can also have:

```java
@Embeddable
public class Address {

    private String city;
    private String pincode;
}
```

Then:

```java
@ElementCollection
private List<Address> addresses;
```

Database:

```text
STUDENT
────────────────
id
name
────────────────

STUDENT_ADDRESS
────────────────
student_id
city
pincode
────────────────
```

Important:

```text
Address @Embeddable
       ↓
Value object
       ↓
Belongs to Student
```

This is different from:

```text
Address @Entity
       ↓
Independent entity
       ↓
Own identity
       ↓
Relationship mapping
```

---

# 🔗 27. Entity Relationships

Now we move from **value mapping** to **entity relationships**.

Suppose:

```text
Student
   │
   └──── Department
```

If Department is an independent entity:

```java
@Entity
public class Department {

    @Id
    private Long id;

    private String name;
}
```

Then Student can reference it:

```java
@ManyToOne
private Department department;
```

Database:

```text
STUDENT
────────────────
id
name
department_id ─────────┐
────────────────        │
                        │
                        ▼
                 DEPARTMENT
                 ─────────────
                 id
                 name
```

The relationship is represented through a **foreign key**.

---

# 28. `@ManyToOne` 👥

Very common relational pattern:

```text
Many Students
      │
      ▼
One Department
```

```java
@ManyToOne
private Department department;
```

Database:

```text
STUDENT
   │
   └── department_id → DEPARTMENT.id
```

---

# 29. `@OneToMany` 👨‍👩‍👧‍👦

The reverse conceptual relationship:

```text
One Department
      │
      ▼
Many Students
```

```java
@OneToMany
private List<Student> students;
```

But this introduces an important concept:

> Which side actually owns the foreign key?

That leads to:

```text
mappedBy
owning side
inverse side
@JoinColumn
```

These should be learned carefully rather than memorized.

---

# 30. `@JoinColumn` 🔗

A relationship can explicitly specify its foreign-key column.

```java
@ManyToOne
@JoinColumn(name = "department_id")
private Department department;
```

Now:

```text
Student.department
       ↓
STUDENT.department_id
       ↓
DEPARTMENT.id
```

---

# 31. `mappedBy` 🔄

Suppose:

```java
@Entity
class Department {

    @OneToMany(mappedBy = "department")
    private List<Student> students;
}
```

and:

```java
@Entity
class Student {

    @ManyToOne
    private Department department;
}
```

`mappedBy = "department"` means:

> The relationship is actually mapped/owned through the `department` field on `Student`.

Important mental model:

```text
Department
    │
    │ mappedBy
    ▼
Student.department
    │
    ▼
department_id
```

This becomes much easier once we understand relational foreign keys first.

---

# 🔀 32. `@OneToOne`

Example:

```text
Person
   │
   └── Passport
```

```java
@OneToOne
private Passport passport;
```

Conceptually:

```text
PERSON
──────────────
id
passport_id
──────────────
      │
      ▼
PASSPORT
──────────────
id
number
──────────────
```

The exact database design depends on which side owns the relationship.

---

# 🔗 33. `@ManyToMany`

Example:

```text
Student
   │
   ├──── Course A
   ├──── Course B
   └──── Course C
```

and:

```text
Course
   │
   ├──── Student 1
   └──── Student 2
```

Relational databases generally represent this using a **join table**:

```text
STUDENT
────────────
id
name

COURSE
────────────
id
name

STUDENT_COURSE
────────────────────
student_id
course_id
────────────────────
```

JPA:

```java
@ManyToMany
private List<Course> courses;
```

The join table can be customized using:

```java
@JoinTable
```

---

# 🧠 34. Value vs Entity — Critical Distinction

This is one of the most important mapping decisions.

### Entity

```text
@Entity
```

Has:

* 🔑 Identity
* 🗃️ Persistent identity
* 🔗 Can participate in relationships
* ♻️ Independent lifecycle conceptually

Example:

```text
Student
Department
Course
```

### Value Object

```text
@Embeddable
```

Has:

* ❌ No independent identity
* 🧩 Belongs to another entity
* 📦 Represents a value

Example:

```text
Address
Money
Name
Coordinates
```

Think:

```text
ENTITY
"I am a thing."

VALUE OBJECT
"I describe something."
```

---

# 🧠 35. The Complete Mapping Picture

At this stage, JPA gives us tools to map almost every basic object-model structure.

```text
                         JAVA MODEL
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
        ▼                    ▼                    ▼
     Entity              Value Object         Collection
        │                    │                    │
      @Entity             @Embeddable       @ElementCollection
        │                    │                    │
        ▼                    ▼                    ▼
      Table              Same Table        Collection Table
        │
        │
        ├── @Id
        │
        ├── @GeneratedValue
        │
        ├── @Column
        │
        ├── @Enumerated
        │
        ├── @Convert
        │
        └── Relationships
               │
       ┌───────┼────────┬──────────┐
       ▼       ▼        ▼          ▼
    @ManyToOne @OneToMany @OneToOne @ManyToMany
```

---

# 🔄 36. Mapping Is Metadata

The annotations aren't themselves performing database operations.

They primarily describe:

```text
"What does this Java model mean
 in the relational model?"
```

For example:

```java
@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Embedded
    private Address address;

    @ElementCollection
    private List<String> phoneNumbers;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
}
```

This gives Hibernate a **mapping model**.

Conceptually:

```text
                 Hibernate
                     │
          reads mapping metadata
                     │
                     ▼
              Builds ORM model
                     │
                     ▼
         understands object ↔ DB
                     │
                     ▼
             persistence work
```

⚠️ **Exactly what Hibernate does with this metadata is the next chapter.**

---

# 🎯 37. What We Have Solved

Let's compare the journey.

### ❌ Raw JDBC

```text
SQL
 ↓
PreparedStatement
 ↓
Parameters
 ↓
Execute
 ↓
ResultSet
 ↓
Manual mapping
```

### 🟡 Spring JdbcTemplate

```text
SQL
 ↓
JdbcTemplate
 ↓
Parameters
 ↓
Result mapping
```

Much JDBC plumbing disappears.

### 🟢 JPA + Hibernate

```text
Java Object Model
       │
       ▼
Mapping Metadata
       │
       ▼
Hibernate ORM
       │
       ▼
JDBC
       │
       ▼
Database
```

Hibernate now understands:

```text
Class       ↔ Table
Field       ↔ Column
ID          ↔ Primary Key
Enum        ↔ DB representation
Value       ↔ Columns
Collection   ↔ Collection Table
Entity      ↔ Entity
Relationship ↔ Foreign Key / Join Table
```

And therefore it can automate much more of the repetitive persistence work.

---

Our next mental model will be:

```text
                    EntityManager
                         │
                         ▼
                Persistence Context
                         │
                         ▼
                   Entity Identity
                         │
                         ▼
                  Managed Objects
                         │
                         ▼
                    Snapshots
                         │
                         ▼
                  Dirty Checking
                         │
                         ▼
                       Flush
                         │
                         ▼
                  SQL Generation
                         │
                         ▼
                       JDBC
                         │
                         ▼
                     Database
```

🔥 **That's where JPA stops being a collection of annotations and starts becoming a system you can reason about.**
