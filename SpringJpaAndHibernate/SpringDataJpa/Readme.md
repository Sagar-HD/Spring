# 🍃 Spring Data JPA — From First Principles

> **Spring Data JPA** is the repository abstraction provided by Spring Data for applications using JPA.
> **The Main Idea:** Instead of implementing repository classes ourselves, we declare repository interfaces and let Spring Data create the implementation automatically, eliminating repetitive boilerplate code while leveraging JPA/Hibernate under the hood.

---

## 1. ❓ Why Spring Data JPA?

Before Spring Data JPA, we had to write custom repository implementations for every single entity:

```java
class StudentRepository {

    Student save(Student student) {
        // entityManager.persist(...)
    }

    Student findById(Long id) {
        // entityManager.find(...)
    }

    void delete(Student student) {
        // entityManager.remove(...)
    }
}

```

For every entity, we repeatedly wrote essentially the same persistence logic. The only parts that changed were:

$$\text{Entity Type} + \text{ID Type}$$

* 🎓 `StudentRepository` $\rightarrow$ `Student` + `Long`
* 🏢 `DepartmentRepository` $\rightarrow$ `Department` + `Long`
* 👤 `ProfileRepository` $\rightarrow$ `Profile` + `Long`

Spring Data recognizes this common pattern and provides a generic repository programming model to eliminate the repetition.

---

## 2. 🌐 Spring Data's Common Repository Model

Spring Data is generic and supports multiple persistence technologies under a unified architecture:

```text
                         Spring Data 🍃
                              │
              "Common Repository Programming Model"
                              │
          ┌───────────────────┼───────────────────┐
          ↓                   ↓                   ↓
   Spring Data JPA      Spring Data MongoDB   Spring Data Redis
          │                   │                   │
          ↓                   ↓                   ↓
      Hibernate          MongoDB Driver       Redis Driver
          │                   │                   │
          ↓                   ↓                   ↓
      SQL Database           MongoDB              Redis

```

$$\text{Common Repository Programming Model} \longrightarrow \text{Spring Data Modules} \longrightarrow \text{Database-Specific Implementation}$$

---

## 3. 🔑 Repository<T, ID>

The absolute foundation of Spring Data is:

```java
public interface Repository<T, ID> { }

```

* **`T`** $\rightarrow$ Domain / Entity type
* **`ID`** $\rightarrow$ Identifier type

```java
public interface StudentRepository extends Repository<Student, Long> { }

```

> 🏷️ **Marker Interface:** `Repository` primarily identifies: *"I am a repository for this domain type and this ID type."* It does not declare CRUD methods itself; its main purpose is to establish the abstraction and enable bean discovery.

---

## 4. 🌳 Repository Hierarchy

```text
                         Repository<T, ID> 🏷️
                          /            \
                         /              \
                        ↓                ↓
               CrudRepository     PagingAndSortingRepository
                     │                     │
                     ↓                     ↓
             ListCrudRepository    ListPagingAndSortingRepository
                     \                     /
                      \                   /
                       └───────┬─────────┘
                               ↓
                        JpaRepository<T, ID> 🚀
                               │
                               ↓
                     StudentRepository

```

```text
Repository
 ├── CRUD capability
 │      ↓
 │  CrudRepository
 │      ↓
 │  ListCrudRepository
 │
 └── Sorting / Pagination capability
        ↓
    PagingAndSortingRepository
        ↓
    ListPagingAndSortingRepository
        ↓
   JpaRepository
        ↓
 Our Repository Interface

```

> 📌 **Key Detail:** `CrudRepository` and `PagingAndSortingRepository` are siblings. `ListPagingAndSortingRepository` extends `PagingAndSortingRepository`, while `ListCrudRepository` extends `CrudRepository`. `JpaRepository` unifies all of these capabilities.

---

## 5. 🛠️ CrudRepository

`CrudRepository<T, ID>` provides generic CRUD operations:

```java
<S extends T> S save(S entity);

<S extends T> Iterable<S> saveAll(Iterable<S> entities);

Optional<T> findById(ID id);

boolean existsById(ID id);

Iterable<T> findAll();

Iterable<T> findAllById(Iterable<ID> ids);

long count();

void deleteById(ID id);

void delete(T entity);

void deleteAllById(Iterable<? extends ID> ids);

void deleteAll(Iterable<? extends T> entities);

void deleteAll();

```

---

## 6. 📋 Why ListCrudRepository?

`CrudRepository` returns `Iterable` for bulk retrieval methods (`Iterable<Student> findAll()`) so it doesn't force a specific collection type across all data stores.

`ListCrudRepository` overrides these methods to return standard Java `List` collections directly:

```text
CrudRepository          ───►  findAll() → Iterable<T>
ListCrudRepository      ───►  findAll() → List<T>

```

---

## 7. 📄 PagingAndSortingRepository

Adds pagination and sorting fragments:

```java
Iterable<T> findAll(Sort sort);

Page<T> findAll(Pageable pageable);

```

---

## 8. 📊 ListPagingAndSortingRepository

Refines sorting to return a `List`:

```text
PagingAndSortingRepository
  ├── findAll(Sort)     ───►  Iterable<T>
  └── findAll(Pageable) ───►  Page<T>

ListPagingAndSortingRepository
  └── findAll(Sort)     ───►  List<T>

```

*(Pagination retains `Page<T>` because it requires evaluation metadata beyond raw items).*

---

## 9. ⚡ JpaRepository

`JpaRepository<T, ID>` is the JPA-specific abstraction combining:

* `CrudRepository`
* `ListCrudRepository`
* `PagingAndSortingRepository`
* `ListPagingAndSortingRepository`
* `QueryByExampleExecutor`

---

## 10. 🎯 JpaRepository Specific Methods

Beyond standard CRUD, `JpaRepository` exposes direct JPA flushing and batch mechanisms:

```java
void flush();

<S extends T> S saveAndFlush(S entity);

<S extends T> List<S> saveAllAndFlush(Iterable<S> entities);

void deleteAllInBatch();

void deleteAllInBatch(Iterable<T> entities);

void deleteAllByIdInBatch(Iterable<ID> ids);

T getReferenceById(ID id);

```

* **`flush()`** $\rightarrow$ Immediately synchronizes pending in-memory changes to the underlying database.
* **`saveAndFlush(entity)`** $\rightarrow$ Saves the entity and flushes changes in a single call.
* **`saveAllAndFlush(entities)`** $\rightarrow$ Batch saves and immediately flushes all entities.
* **`deleteAllInBatch()`** $\rightarrow$ Executes a single bulk SQL `DELETE` query rather than loading and removing entities individually.
* **`getReferenceById(id)`** $\rightarrow$ Returns a lazy JPA proxy reference without triggering an immediate SQL `SELECT`.

---

## 11. ✍️ Our Repository Interface

We write simply:

```java
public interface StudentRepository extends JpaRepository<Student, Long> {
}

```

No implementation class (`StudentRepositoryImpl`), no explicit `entityManager.persist()`, `find()`, `merge()`, or `remove()`.

---

## 12. 🔮 How Does Spring Give Us an Implementation?

At startup, Spring detects repository interfaces and dynamically creates a **JDK Dynamic Proxy**:

```text
Application ──► StudentRepository (interface) ──► Spring Proxy Generator
                                                         │
                                                         ▼
                                                  Repository Proxy
                                                         │
               ┌─────────────────────────────────────────┴─────────────────────────────────────────┐
               ▼                                                                                   ▼
   Standard Repository Operations                                                         Derived / Custom Queries
               │                                                                                   │
               ▼                                                                                   ▼
      SimpleJpaRepository                                                            Spring Query Infrastructure

```

---

## 13. ⚙️ SimpleJpaRepository

The default target implementation backing every JPA repository proxy is `SimpleJpaRepository<T, ID>`.

Because it relies on generics, the same class powers `StudentRepository`, `DepartmentRepository`, and `ProfileRepository` seamlessly.

---

## 14. 💾 What Happens When We Call `save()`?

```text
studentRepository.save(student)
       │
       ▼
 Repository Proxy
       │
       ▼
SimpleJpaRepository
       │
       ▼
 Entity Manager ───► New Entity?      ───► entityManager.persist(student)
                 └───► Existing Entity? ───► entityManager.merge(student)

```

*(Entity newness is determined via Spring Data JPA's entity-newness detection strategy, such as checking null `@Id` fields).*

---

## 15. 📦 `saveAll()` Execution

```text
saveAll() ──► Iterate Entities ──► save(entity) ──► persist() / merge()

```

> ⚠️ **Note:** `saveAll()` is not a single magical SQL operation. Actual database batching depends on vendor driver configuration and Hibernate configuration settings (`hibernate.jdbc.batch_size`).

---

## 16. 🔎 `findById()` Execution

```text
findById(10L) ──► SimpleJpaRepository ──► EntityManager.find(...) ──► Persistence Context / Database

```

---

## 17. 🗑️ `delete()` Execution

```text
delete(student) ──► SimpleJpaRepository ──► EntityManager.remove(student) ──► Database

```

---

## 18. 🔀 Standard Methods vs. Custom Methods

What happens when we add custom declarations like:

```java
List<Student> findByName(String name);

```

`SimpleJpaRepository` does not contain a `findByName` method. Spring Data resolves this using **Query Derivation**.

---

## 19. 🧩 Query Derivation

Spring parses the method signature directly:

```text
        find      By      Name
         │        │        │
         ▼        ▼        ▼
     Operation Delimiter Property Filter

```

---

## 20. ⚖️ Subject + Predicate Breakdown

```text
               findByNameAndAge
               ───────┬────────
                Subject + Predicate

```

* **`find`** $\rightarrow$ Subject (Introducing Keyword)
* **`ByNameAndAge`** $\rightarrow$ Predicate (Criteria logic separated by `By`)

---

## 21. 🔍 Query Subject Keywords

| Subject Keyword | Operation Meaning | Example |
| --- | --- | --- |
| `find...By`, `read...By`, `get...By`, `query...By`, `search...By` | Find matching entities | `List<Student> findByName(String name);` |
| `stream...By` | Stream matching results | `Stream<Student> streamByName(String name);` |
| `count...By` | Count matching entities | `long countByDepartment(String dept);` |
| `exists...By` | Existence check | `boolean existsByEmail(String email);` |
| `delete...By`, `remove...By` | Delete matching entities | `long deleteByName(String name);` |

---

## 22. ⚙️ Predicate Keywords

| Keyword Pattern | Example Method | Equivalent SQL Clause |
| --- | --- | --- |
| **Equals** | `findByName(String name)`, `findByNameIs`, `findByNameEquals` | `WHERE name = ?` |
| **Not Equal** | `findByNameNot(String name)`, `findByNameIsNot` | `WHERE name <> ?` |
| **Greater Than** | `findByAgeGreaterThan(int age)` | `WHERE age > ?` |
| **Greater Than Equal** | `findByAgeGreaterThanEqual(int age)` | `WHERE age >= ?` |
| **Less Than** | `findByAgeLessThan(int age)` | `WHERE age < ?` |
| **Less Than Equal** | `findByAgeLessThanEqual(int age)` | `WHERE age <= ?` |
| **Between** | `findByAgeBetween(int min, int max)` | `WHERE age BETWEEN ? AND ?` |
| **In / Not In** | `findByNameIn(List<String> names)` | `WHERE name IN (...)` |
| **Null Checks** | `findByEmailIsNull()`, `findByEmailIsNotNull()` | `WHERE email IS NULL` |

---

## 23. 🔤 String Matching Keywords

| Keyword | Method Example | SQL Equivalent |
| --- | --- | --- |
| **Like** | `findByNameLike(String pattern)` | `WHERE name LIKE ?` |
| **StartingWith** | `findByNameStartingWith(String prefix)` | `WHERE name LIKE 'Sagar%'` |
| **EndingWith** | `findByNameEndingWith(String suffix)` | `WHERE name LIKE '%Sagar'` |
| **Containing** | `findByNameContaining(String infix)` | `WHERE name LIKE '%Sagar%'` |

---

## 24. 🔠 Case Insensitivity

```java
List<Student> findByNameIgnoreCase(String name);

```

Ignores casing differences during comparisons (e.g., matching `"sagar"`, `"Sagar"`, and `"SAGAR"`).

---

## 25. 🔗 Combining Predicates

Combine conditions using `And` or `Or`:

```java
List<Student> findByNameAndAge(String name, int age); // WHERE name = ? AND age = ?
List<Student> findByNameOrAge(String name, int age);  // WHERE name = ? OR age = ?

```

---

## 26. 🧭 Nested Property Traversal

Traverse object relationships directly using entity field paths:

```java
// Student -> department -> name
List<Student> findByDepartmentName(String name);

```

---

## 27. 🔝 Limiting Results

Restrict returning result sets using `First` or `Top`:

```java
Student findFirstByName(String name);
List<Student> findTop3ByDepartment(String department);

```

---

## 28. 📶 Sorting via Method Names

Append `OrderBy` directly to derived queries:

```java
List<Student> findByDepartmentOrderByAgeAsc(String department);
List<Student> findByDepartmentOrderByAgeDesc(String department);

```

---

## 29. ⚠️ When Method Names Become Too Long

```java
// ❌ Hard to read, maintain, and debug:
findByDepartmentAndAgeGreaterThanAndNameContainingIgnoreCaseAndEmailIsNotNullOrderByAgeDesc(...)

```

$$\text{Derived Query Too Long} \longrightarrow \text{Unmaintainable} \longrightarrow \text{Switch to } \texttt{@Query}$$

---

## 30. 💡 `@Query` Annotation

Write queries directly over interface methods:

```java
@Query("""
       SELECT s
       FROM Student s
       WHERE s.name = :name
       """)
List<Student> findStudents(@Param("name") String name);

```

---

## 31. 🏷️ Named Parameters

Bind explicit method arguments using `@Param`:

```text
:name  ◄────────►  @Param("name")

```

---

## 32. 📖 JPQL (Java Persistence Query Language)

`@Query` defaults to **JPQL**, which operates on Java entities rather than database tables:

```text
JPQL Query ──► JPA ──► Hibernate ──► SQL ──► Database

```

---

## 33. ⚔️ JPQL vs. SQL Comparison

| Feature | Database SQL | Object JPQL |
| --- | --- | --- |
| **Target** | Tables & Columns (`student`, `student_name`) | Entities & Fields (`Student s`, `s.name`) |
| **Example** | `SELECT * FROM student WHERE student_name = ?` | `SELECT s FROM Student s WHERE s.name = :name` |

---

## 34. 🔗 JPQL Joins

Join over entity relationship properties:

```java
@Query("""
       SELECT s
       FROM Student s
       JOIN s.department d
       WHERE d.name = :departmentName
       """)
List<Student> findStudentsByDepartment(@Param("departmentName") String departmentName);

```

---

## 35. 🔀 Explicit vs. Implicit Joins

* **Explicit Join:**
```jpql
SELECT s FROM Student s JOIN s.department d WHERE d.name = :name

```


* **Implicit Join (Path Navigation):**
```jpql
SELECT s FROM Student s WHERE s.department.name = :name

```



---

## 36. ⚡ Native SQL Queries

Bypass JPQL to run database-native SQL queries using `nativeQuery = true` or `@NativeQuery`:

```java
@Query(
    value = "SELECT * FROM student WHERE student_name = :name",
    nativeQuery = true
)
List<Student> findByNameNative(@Param("name") String name);

```

---

## 37. 🚦 Query Strategy Decision Tree

```text
                    Need a query?
                          │
                          ▼
            Can method name express it clearly?
                       ┌──┴──┐
                     YES     NO
                      │       │
                      ▼       ▼
                Derived     @Query
               Query          │
                              ▼
                            JPQL
                              │
               Is database-specific SQL required?
                            ┌──┴──┐
                          NO     YES
                           │       │
                           ▼       ▼
                         JPQL    Native SQL

```

---

## 38. 🔀 Sorting via `Sort`

Pass dynamic sorting criteria programmatically:

```java
Sort sort = Sort.by("age").descending()
                .and(Sort.by("name").ascending());

List<Student> students = repository.findAll(sort);
// Generates: ORDER BY age DESC, name ASC

```

---

## 39. 📑 Pagination via `Pageable`

Request specific zero-indexed data chunks efficiently:

```java
Pageable pageable = PageRequest.of(0, 10); // Page 0, Size 10

```

---

## 40. 🧮 How Pagination Works Conceptually

$$\text{Offset} = \text{Page Number} \times \text{Page Size}$$

For `PageRequest.of(2, 10)`:

$$\text{Offset} = 2 \times 10 = 20 \quad \Longrightarrow \quad \text{Skip first 20 records, fetch next 10}$$

---

## 41. 🛠️ Combining Pagination & Sorting

```java
Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
Page<Student> page = repository.findAll(pageable);

```

---

## 42. 📦 The `Page<T>` Object

Returns slice content alongside evaluation metadata:

$$\text{Page<T>} = \text{Actual Data (List<T>)} + \text{Pagination Metadata}$$

---

## 43. 📊 Essential `Page` Methods

```java
List<Student> content = page.getContent(); // Retrieved entities
int pageNum           = page.getNumber();   // Current page index
int size              = page.getSize();      // Requested page size
long totalElements    = page.getTotalElements(); // Total records across all pages
int totalPages        = page.getTotalPages();    // Total pages count

boolean hasNext       = page.hasNext();
boolean isFirst       = page.isFirst();
boolean isEmpty       = page.isEmpty();

```

---

## 44. ⚡ `Page<T>` vs. `Slice<T>`

```text
Slice<T>  ──► Contains content + hasNext() indicator (No count query overhead)
Page<T>   ──► Contains content + hasNext() + totalElements + totalPages (Requires extra count query)

```

$$\text{Page Request} \longrightarrow \text{Query 1: Fetch Data} + \text{Query 2: Count Total Records}$$

---

## 45. 🖼️ Complete Repository Architecture

```text
                 OUR CODE
                    │
                    ▼
       StudentRepository interface
                    │ (extends)
                    ▼
            JpaRepository<T, ID>
                    │
                    ▼
          Spring Data Infrastructure
                    │
                    ▼
             Proxy Object
                    │
          ┌─────────┴──────────┐
          ▼                    ▼
 Standard Methods       Query Methods
          │                    │
          ▼                    ▼
 SimpleJpaRepository    Query Derivation / @Query
          │                    │
          └──────────┬─────────┘
                     ▼
                JPA / EntityManager
                     │
                     ▼
                  Hibernate ──► SQL ──► Database

```

---

## 46. 💡 What Spring Data JPA Eliminates

```text
BEFORE: Interface ──► Implementation ──► EntityManager ──► Manual Queries ──► Boilerplate
AFTER:  Interface ──► Spring Data ──► Done 🚀

```

---

## 47. 🎯 The Three Levels of Repository Methods

```text
Level 1: Predefined Methods   ──► save(), findById(), findAll(), delete(), flush()
Level 2: Derived Methods      ──► findByNameAndAge(String name, int age)
Level 3: Manual @Query        ──► @Query("SELECT s FROM Student s WHERE s.name = :name")

```

---

## 48. 🌐 The Big Picture Flow

```text
                                 Spring Data JPA 🍃
                                        │
                                        ▼
                               Repository Abstraction
                                        │
                                        ▼
                                Repository<T, ID>
                                        │
                       ┌────────────────┴────────────────┐
                       ▼                                 ▼
                 Standard Methods                  Query Methods
                       │                                 │
                       ▼                                 ▼
                JpaRepository                  Method Derivation / @Query
                       │                                 │
                       ▼                                 ▼
              SimpleJpaRepository                 JPQL / Native SQL
                       │                                 │
                       └────────────────┬────────────────┘
                                        ▼
                                JPA / Hibernate
                                        │
                                        ▼
                                 SQL / Database

```

---

## 49. ⚡ Quick Revision Reference

* **`Repository<T, ID>`** $\rightarrow$ Core marker abstraction.
* **`CrudRepository`** $\rightarrow$ Generic CRUD capabilities returning `Iterable`.
* **`ListCrudRepository`** $\rightarrow$ Standard CRUD capabilities returning `List`.
* **`PagingAndSortingRepository`** $\rightarrow$ Pagination & sorting features.
* **`ListPagingAndSortingRepository`** $\rightarrow$ Sorting returning `List`.
* **`JpaRepository`** $\rightarrow$ Combines all above plus JPA batching/flushing operations.

---

## 50. 🧠 Core Mental Model

1. **You write:** `interface StudentRepository extends JpaRepository<Student, Long>`
2. **Spring generates:** Dynamic runtime Proxy.
3. **Execution paths:**
* **Standard CRUD** $\rightarrow$ Proxy forwards to `SimpleJpaRepository`.
* **Derived Queries** $\rightarrow$ Proxy forwards to Query Derivation engine.
* **`@Query`** $\rightarrow$ Proxy executes declared JPQL or Native SQL.


4. **底层 Engine:** Everything maps cleanly to **JPA $\rightarrow$ Hibernate $\rightarrow$ SQL $\rightarrow$ Database**.