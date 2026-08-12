# Spring JDBC: From Plain JDBC to `JdbcTemplate` 🚀

When interacting with databases in Java, your true objective is straightforward: **send SQL, bind parameters, and map the results**.

Plain JDBC forces developers to manage tedious connection routines, statement creation, and resource cleanup. **Spring JDBC** solves this by leveraging the **Template Method Pattern**—handling all the repetitive infrastructure plumbing while leaving you in total control of your SQL.

---

## 1. The Problem: 90% Plumbing, 10% Purpose 😫

In **Plain JDBC**, running even a simple query requires a repetitive 9-step ceremony:

```text
 1. Open Connection ──► 2. Create PreparedStatement ──► 3. Bind Parameters
                                                                │
 6. Close Statement ◄── 5. Close Connection ◄── 4. Execute & Process ResultSet
                                                                │
 7. Handle SQLException ──► 8. Clean up in Finally ──► 9. Map Java Objects

```

### Plain JDBC vs. Spring JDBC Code Comparison 🔍

```java
// ❌ PLAIN JDBC: ~25 lines of noisy boilerplate for ONE object
public Student findById(int id) {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
        conn = dataSource.getConnection();
        ps = conn.prepareStatement("SELECT * FROM student WHERE id = ?");
        ps.setInt(1, id);
        rs = ps.executeQuery();
        if (rs.next()) {
            Student s = new Student();
            s.setId(rs.getInt("id"));
            s.setName(rs.getString("name"));
            s.setAge(rs.getInt("age"));
            return s;
        }
        return null;
    } catch (SQLException e) {
        throw new RuntimeException("Database error", e);
    } finally {
        // Resource leaks happen if any of these close calls fail!
        try { if (rs != null) rs.close(); } catch (SQLException e) {}
        try { if (ps != null) ps.close(); } catch (SQLException e) {}
        try { if (conn != null) conn.close(); } catch (SQLException e) {}
    }
}

```

```java
// ✅ SPRING JDBC: 1 clean expression!
public Student findById(int id) {
    String sql = "SELECT * FROM student WHERE id = ?";
    return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Student.class), id);
}

```

---

## 2. The Solution: Template Method Pattern 🏗️

Spring JDBC extracts the fixed workflow into `JdbcTemplate`. The template class owns the database lifecycle, while you supply only the variable parts.

```text
                      Spring's JdbcTemplate 🏛️
                                 │
         ┌───────────────────────┴───────────────────────┐
         ▼                                               ▼
  FIXED ALGORITHM (Spring)                      VARIABLE PARTS (You)
  ├── 🔌 Acquire Connection from Pool            ├── 📜 SQL Query
  ├── 📜 Prepare Statement                       ├── 🎯 Binding Parameters
  ├── ⚡ Execute SQL                             └── 🧩 Result Mapping (RowMapper)
  ├── 🛡️ Translate SQLExceptions
  └── 🧹 Close Resources

```

---

## 3. Connection Pooling with HikariCP 🏊‍♂️

Creating physical database connections over the network is expensive 🐢. Connection pools maintain a set of pre-established, reusable connections.

```text
                  HikariCP Connection Pool 🏊
        ┌───────────────────────────────────────────────┐
        │  🟢 Connection 1 (Idle)   🟢 Connection 2 (Idle) │
        │  🟠 Connection 3 (Active) 🟢 Connection 4 (Idle) │
        └───────────────────────────────────────────────┘
                                 ▲
  connection.close()             │ borrow connection
  returns connection ────────────┘
  back to pool!

```

> 💡 **Key Takeaway:** When calling `connection.close()` inside a pooled environment, the physical connection is **returned to the pool**, not destroyed.

---

## 🎮 Interactive Spring JDBC Flow Visualizer

Use this interactive playground to simulate how `JdbcTemplate` manages HikariCP connections, maps rows, and handles exception translation under the hood:

---

## 4. The Core Operations Triad 🛠️

`JdbcTemplate` relies on three primary method patterns for database interactions:

```text
 🎯 update()          ──► INSERT, UPDATE, DELETE (Returns int count of affected rows)
 📚 query()           ──► SELECT returning multiple rows (Returns List<T>)
 🎯 queryForObject()  ──► SELECT returning exactly 1 row or scalar value (Returns T)

```

### Method Examples

```java
// 1️⃣ UPDATE (Insert, Update, Delete)
String insertSql = "INSERT INTO student (name, age) VALUES (?, ?)";
int rowsAffected = jdbcTemplate.update(insertSql, "Sagar", 22);

// 2️⃣ QUERY (Multiple Rows)
String selectAllSql = "SELECT * FROM student WHERE age >= ?";
List<Student> students = jdbcTemplate.query(
    selectAllSql,
    (rs, rowNum) -> new Student(rs.getInt("id"), rs.getString("name"), rs.getInt("age")),
    18
);

// 3️⃣ QUERY FOR OBJECT (Single Row or Scalar Value)
String countSql = "SELECT COUNT(*) FROM student";
Integer totalStudents = jdbcTemplate.queryForObject(countSql, Integer.class);

```

---

## 5. Result Mapping: `RowMapper` vs `BeanPropertyRowMapper` 🧩

How does database tabular data convert into Java objects?

### Custom `RowMapper<T>` ✍️

Gives you full control over field conversions and complex logic:

```java
RowMapper<Student> customMapper = (rs, rowNum) -> {
    Student student = new Student();
    student.setId(rs.getInt("id"));
    student.setName(rs.getString("name"));
    student.setAge(rs.getInt("age"));
    return student;
};

```

### `BeanPropertyRowMapper<T>` 🪄

Automatically matches database snake_case columns to Java camelCase properties:

```text
Database Column           Java Property
student_id        ──►     studentId
first_name        ──►     firstName
age               ──►     age

```

```java
List<Student> students = jdbcTemplate.query(
    "SELECT * FROM student",
    new BeanPropertyRowMapper<>(Student.class)
);

```

---

## 6. Exception Translation 🛡️

Plain JDBC forces vendor-specific checked exceptions (`SQLException`). Spring translates raw database errors into a unified, **unchecked** exception hierarchy rooted at `DataAccessException`.

```text
                   SQLException (Checked, Vendor-Specific)
                                      │
                                      ▼
             Spring Exception Translator (SQLExceptionTranslator)
                                      │
                                      ▼
               DataAccessException (Unchecked, Vendor-Agnostic)
        ┌─────────────────────────────┼─────────────────────────────┐
        ▼                             ▼                             ▼
DuplicateKeyException   EmptyResultDataAccessException   BadSqlGrammarException
 (e.g., PK Violation)       (queryForObject returned 0)     (e.g., Syntax Error)

```

---

## 7. The Abstraction Ladder 🧗

Where does Spring JDBC sit compared to raw JDBC and full ORMs like Hibernate/JPA?

| Framework | SQL Control | Boilerplate | Overhead | Primary Use Case |
| --- | --- | --- | --- | --- |
| **Plain JDBC** 🛠️ | Direct | High 💥 | Minimal ⚡ | Legacy applications, lightweight scripts |
| **Spring JDBC** 🚀 | Direct | Minimal 🧹 | Very Low 🏎️ | Performance-critical SQL, complex reporting queries |
| **JPA / Hibernate** 🔮 | Abstracted (JPQL) | Near Zero ✨ | Medium 🏋️ | Rapid domain CRUD operations & complex entity models |

---