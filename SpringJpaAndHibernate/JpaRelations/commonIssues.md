
## ORM Entity Design Checklist

### 1. First decide the relationship

Ask:

> **What is the real ownership/lifecycle relationship in the domain?**

| Relationship          | Typical mapping |
| --------------------- | --------------- |
| Student → Department  | `@ManyToOne`    |
| Department → Students | `@OneToMany`    |
| Student ↔ Course      | `@ManyToMany`   |
| Student ↔ Profile     | `@OneToOne`     |

Don't choose cascade/orphan first. **Understand the relationship first.**

---

### 2. Decide the owning side

For bidirectional relationships, one side owns the FK.

Example:

```java
@ManyToOne
@JoinColumn(name = "department_id")
private Department department;
```

This is the **owning side** because `students.department_id` is the FK.

The other side:

```java
@OneToMany(mappedBy = "department")
private List<Student> students;
```

is the inverse side.

Remember:

```text
mappedBy = Java field name on the owning side
```

So:

```java
mappedBy = "department"
```

because Student has:

```java
private Department department;
```

---

### 3. Bidirectional ≠ automatically synchronized

JPA doesn't maintain both Java references for you.

Do:

```java
student.setDepartment(department);
department.getStudents().add(student);
```

A helper method is better:

```java
public void addStudent(Student student) {
    students.add(student);
    student.setDepartment(this);
}
```

Use the helper everywhere.

---

### 4. Decide the lifecycle

This is the **big one for cascade**.

Ask:

> **Does the child have meaning independently of the parent?**

#### Strong ownership

```text
Student → Profile
```

If Profile belongs exclusively to Student:

```java
@OneToOne(
    cascade = CascadeType.ALL,
    orphanRemoval = true
)
```

Reason:

```text
Student created → Profile created
Student deleted → Profile deleted
Profile removed from Student → Profile deleted
```

That's a good `ALL + orphanRemoval` candidate.

#### Independent entity

```text
Student → Department
```

A Department exists independently and has many students.

Don't blindly cascade everything from Student to Department.

---

### 5. Choose cascade based on the operation

Don't think:

> "This is bidirectional, therefore cascade both sides."

Think:

> **"If I perform this operation on A, should Hibernate perform it on B?"**

For example:

```java
cascade = CascadeType.PERSIST
```

means:

```text
persist(A)
   ↓
persist(B)
```

`CascadeType.MERGE`:

```text
merge(A)
   ↓
merge(B)
```

`CascadeType.REMOVE`:

```text
remove(A)
   ↓
remove(B)
```

And:

```java
CascadeType.ALL
```

means all cascade operations.

---

### 6. Be VERY careful with `CascadeType.REMOVE`

This is dangerous on shared relationships.

For:

```text
Department
 ├── Student A
 ├── Student B
 └── Student C
```

you generally don't want:

```java
@ManyToOne(cascade = CascadeType.REMOVE)
private Department department;
```

because deleting one Student could attempt to delete the Department.

And then other Students are still referencing it. 💀

`REMOVE` usually belongs on relationships where the child is truly owned by the parent.

---

### 7. `orphanRemoval` is about collection/reference membership

Think:

> **"If this child is removed from the parent's relationship, should the child itself be deleted?"**

Example:

```java
@OneToOne(orphanRemoval = true)
private Profile profile;
```

Then:

```java
student.setProfile(null);
```

can cause the old Profile to be deleted.

For a collection:

```java
@OneToMany(
    mappedBy = "student",
    orphanRemoval = true
)
private List<Address> addresses;
```

Removing an Address from the collection can delete that Address.

### Important distinction

```text
cascade REMOVE
    ↓
parent is deleted
    ↓
child is deleted
```

while:

```text
orphanRemoval
    ↓
child is removed from relationship
    ↓
child is deleted
```

---

### 8. Don't use `orphanRemoval` on shared entities

For:

```text
Department ← Student → Course
```

Courses are shared.

If Student A removes Course X, you **do not** want Course X deleted from the database because Student B may still use it.

So `orphanRemoval` is generally inappropriate for `@ManyToMany`.

---

### 9. Be careful with `ManyToMany`

For:

```text
Student ↔ Course
```

usually:

```java
@ManyToMany
@JoinTable(...)
private Set<Course> courses;
```

Avoid:

```java
@ManyToMany(cascade = CascadeType.ALL)
```

unless you are **absolutely sure** about the lifecycle.

Why?

```text
Student A → Java
Student B → Java
```

If you remove/delete Student A, you don't want Hibernate deleting the shared `Java` Course.

Usually the **join-table row** should disappear, not the Course itself.

---

### 10. Prefer `Set` for many-to-many

Usually:

```java
private Set<Course> courses = new HashSet<>();
```

rather than:

```java
private List<Course> courses;
```

because conceptually:

```text
Student is enrolled in Course
```

is a set of relationships and duplicate relationships don't normally make sense.

---

### 11. Initialize collections

Always:

```java
private List<Student> students = new ArrayList<>();
```

or:

```java
private Set<Course> courses = new HashSet<>();
```

Never rely on:

```java
private List<Student> students;
```

and then expect:

```java
students.add(...)
```

to work.

---

### 12. Think about `LAZY` vs `EAGER`

For relationships, especially collections, don't casually load the whole graph.

Generally prefer:

```java
@OneToMany(fetch = FetchType.LAZY)
```

and:

```java
@ManyToMany(fetch = FetchType.LAZY)
```

Be particularly careful with `EAGER`, because:

```text
Department
 ↓
Students
 ↓
Courses
 ↓
...
```

can turn one simple operation into a huge object graph.

---

### 13. Don't return entities directly from REST

Your entity graph might be:

```text
Department ↔ Student ↔ Course
```

Returning entities directly can cause:

* recursive JSON
* huge responses
* lazy-loading problems
* accidental exposure of fields
* tight coupling between DB model and API

Use:

```text
Entity
   ↓
Mapper
   ↓
DTO
```

and use summary DTOs for relationships.

---

### 14. Keep generated IDs nullable

Use:

```java
private Long id;
```

not:

```java
private long id;
```

for generated JPA IDs.

New entity:

```text
id = null
```

After persistence:

```text
id = generated value
```

This makes entity state much clearer.

---

## The mental checklist I'd actually use

Before adding an association, ask these **7 questions**:

```text
1. What is the relationship?
        ↓
2. Who owns the FK?
        ↓
3. Is it unidirectional or bidirectional?
        ↓
4. Does the child have an independent lifecycle?
        ↓
5. Should PERSIST/MERGE/REMOVE cascade?
        ↓
6. Should removing the relationship delete the child?
        ↓
7. Is the relationship shared or exclusive?
```

Then choose:

```text
                 Relationship
                      │
          ┌───────────┴───────────┐
          ↓                       ↓
    Independent              Owned child
          │                       │
     little/no cascade       cascade as needed
          │                       │
    no orphanRemoval         orphanRemoval?
```

### Your current three relationships

I'd think about them like this:

```text
Department ─────── Student
   1                  *
   
Independent entities
→ Student owns FK
→ usually no REMOVE cascade
→ no orphanRemoval
```

```text
Student ─────────── Profile
    1                  1

Exclusive lifecycle
→ cascade ALL can make sense
→ orphanRemoval can make sense
```

```text
Student ─────────── Course
    *                  *

Shared entities
→ join table
→ avoid REMOVE/orphanRemoval
→ be cautious with ALL
→ usually Set
```

If you get these lifecycle decisions right **before writing the annotations**, JPA becomes dramatically less painful. The annotations are basically just expressing the decisions you've already made.
