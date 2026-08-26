# 🔐 Optimistic vs Pessimistic Locking

> **Goal:** Understand how databases handle concurrent access to the same resource and how to choose between optimistic locking, pessimistic locking, and atomic database operations.

---

## 1. The Problem: Concurrent Access

Suppose we have one seat:

```text
Seat A1
booked = false
```

Two users request it at almost the same time:

```text
User A ──┐
         ├──→ Seat A1
User B ──┘
```

Without concurrency control:

```text
A → reads booked = false
B → reads booked = false

A → booked = true
B → booked = true

❌ Both believe they booked the seat
```

The database needs a mechanism to ensure the **business invariant**:

```text
One seat → one successful booking
```

---

# 2. Optimistic Locking ⚡

### Mental model

> **"Don't lock the row initially. Let transactions work, but detect if someone changed the data before I save."**

JPA provides optimistic locking through:

```java
@Version
private Long version;
```

Example:

```java
@Entity
public class Seat {

    @Id
    private Long id;

    private boolean booked;

    @Version
    private Long version;
}
```

Suppose:

```text
booked = false
version = 5
```

Two transactions read it:

```text
Transaction A → version 5
Transaction B → version 5
```

A saves first:

```text
booked = true
version = 6
```

Hibernate conceptually performs:

```sql
UPDATE seats
SET booked = true,
    version = 6
WHERE id = 1
AND version = 5;
```

A succeeds.

B tries to update using:

```text
version = 5
```

But the database now has:

```text
version = 6
```

Therefore:

```text
0 rows updated
       ↓
OptimisticLockException
```

### Important

`@Version` handles **conflict detection**.

Your application decides what happens afterward:

```text
Conflict
 ├── fail
 ├── ask user to retry
 └── automatically retry
```

---

# 3. Optimistic Retry 🔄

A conflict doesn't necessarily mean permanent failure.

```text
A → version 5 → succeeds → version 6

B → version 5 → conflict
             ↓
          retry
             ↓
       read version 6
             ↓
       try again
```

But never blindly retry forever.

Use:

```text
Maximum attempts
+
backoff
+
jitter
```

Example:

```text
Attempt 1 → conflict
     ↓
wait 10ms

Attempt 2 → conflict
     ↓
wait 20ms

Attempt 3 → success / business failure
```

### ⚠️ High contention problem

Suppose:

```text
10,000 requests
      ↓
same row
```

You can get:

```text
read
 ↓
conflict
 ↓
retry
 ↓
read
 ↓
conflict
 ↓
retry
```

This can create a **thundering herd**.

Therefore optimistic locking isn't automatically the best solution for extremely hot resources.

---

# 4. Pessimistic Locking 🔒

### Mental model

> **"I expect contention, so acquire a database lock before modifying the resource."**

With JPA:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<Seat> findById(Long id);
```

Or using PostgreSQL:

```sql
SELECT *
FROM seats
WHERE id = ?
FOR UPDATE;
```

Example:

```java
@Transactional
public void bookSeat(Long seatId) {

    Seat seat = repository.findAndLockSeat(seatId);

    if (seat.isBooked()) {
        throw new RuntimeException("Already booked");
    }

    seat.setBooked(true);
}
```

Flow:

```text
Transaction A
      ↓
SELECT ... FOR UPDATE
      ↓
🔒 Seat locked
      ↓
booked = true
      ↓
COMMIT
      ↓
🔓 unlock
```

Meanwhile:

```text
Transaction B
      ↓
SELECT ... FOR UPDATE
      ↓
⏳ waits
```

After A commits:

```text
B gets lock
      ↓
reads latest state
      ↓
booked = true
      ↓
reject
```

---

# 5. Pessimistic ≠ Java `synchronized`

This is important.

### Java lock

```java
synchronized
```

protects memory inside **one JVM**.

```text
App Instance 1
    ↓
Java memory
    ↓
🔒
```

It does not protect against:

```text
App Instance 2
App Instance 3
```

### Database lock

```sql
SELECT ... FOR UPDATE
```

protects the database resource across application instances.

```text
App 1 ──┐
App 2 ──┼──→ PostgreSQL 🔒
App 3 ──┘
```

---

# 6. Optimistic vs Pessimistic

|                            | Optimistic       | Pessimistic             |
| -------------------------- | ---------------- | ----------------------- |
| Initial lock               | ❌                | ✅                       |
| Conflict detection         | Version          | Database lock           |
| Other transaction          | Can proceed/read | May wait                |
| Typical mechanism          | `@Version`       | `FOR UPDATE`            |
| Conflict handling          | Fail/retry       | Wait                    |
| Good for                   | Lower conflict   | High contention         |
| DB lock held while working | Usually no       | Yes, during transaction |
| Main risk                  | Retry storms     | Lock contention         |

---

# 7. The Critical Rule for Pessimistic Locking

Keep the transaction **short**.

### ✅ Good

```text
BEGIN
 ↓
lock row
 ↓
check
 ↓
update
 ↓
COMMIT
```

### ❌ Bad

```text
BEGIN
 ↓
lock row
 ↓
call payment gateway
 ↓
wait 30 seconds
 ↓
send email
 ↓
COMMIT
```

Never hold a database lock while waiting for a user or external service.

---

# 8. Atomic Database Update ⚡

Optimistic and pessimistic locking aren't your only options.

For simple inventory, an atomic SQL operation can be excellent:

```sql
UPDATE products
SET available_stock = available_stock - 1
WHERE id = ?
AND available_stock > 0;
```

Then:

```text
affected rows = 1
        ↓
purchase succeeded

affected rows = 0
        ↓
out of stock
```

The database handles the concurrent update atomically.

This can be preferable to optimistic retries when thousands of users compete for the same inventory row.

---

# 9. How to Choose?

Don't memorize:

> "Optimistic = low concurrency, pessimistic = high concurrency."

Instead ask:

### Question 1 — How frequently will conflicts occur?

```text
Low conflict
    ↓
Optimistic is attractive
```

```text
High conflict
    ↓
Consider pessimistic / atomic operation
```

---

### Question 2 — Can I safely retry?

If yes:

```text
Optimistic
```

becomes more attractive.

If no:

```text
Pessimistic
```

or another coordination strategy may be better.

---

### Question 3 — Is the critical operation short?

If yes:

```text
Pessimistic locking
```

can be perfectly reasonable.

If the operation involves:

```text
Payment
User interaction
External API
Long processing
```

don't hold the DB lock across it.

---

### Question 4 — Is this an extremely hot resource?

For:

```text
10,000 users
      ↓
same inventory
```

consider:

```text
Atomic SQL
Redis
Reservation
Queueing
Admission control
```

depending on the actual requirements.

---

# 10. Reservation Is Different 🎟️

A **lock** and a **reservation** solve different problems.

### Lock

> "I'm modifying this database state right now."

Usually lasts:

```text
milliseconds
```

### Reservation

> "This resource temporarily belongs to this user."

Could last:

```text
10 minutes
```

Example:

```text
AVAILABLE
    ↓
RESERVED
    ↓
PAYMENT
    ↓
CONFIRMED
```

You **do not** keep a database lock for those 10 minutes.

---

# 11. Flash Sale Example 🛒

Suppose:

```text
Stock = 100
Users = 10,000
```

You could start with:

```text
10,000 requests
       ↓
atomic conditional UPDATE
       ↓
100 successful
```

Or investigate:

```text
Optimistic locking
       ↓
conflicts
       ↓
bounded retries
```

Or:

```text
Pessimistic locking
       ↓
transactions serialize inventory updates
```

Then add reservation if the business flow includes:

```text
BUY
 ↓
PAYMENT
 ↓
CONFIRMATION
```

---

# 12. Event Booking Example 🎟️

For a seat:

```text
A12
 ↓
AVAILABLE
 ↓
RESERVED for User A
 ↓
10-minute payment window
 ↓
CONFIRMED
```

Reservation protects **business ownership**.

The DB lock/optimistic mechanism protects the **short state transition** when the reservation is created or confirmed.

---

# 13. The Big Mental Model

```text
                    CONCURRENT ACCESS
                           │
             ┌─────────────┼─────────────┐
             ↓             ↓             ↓
        Optimistic     Pessimistic    Atomic SQL
             │             │             │
          @Version      FOR UPDATE    UPDATE ... WHERE
             │             │             │
          detect         prevent       atomic
          conflict       conflict      operation
             │             │             │
             └─────────────┼─────────────┘
                           ↓
                     DB correctness
```

And separately:

```text
                 BUSINESS LIFECYCLE
                        │
                   Reservation
                        │
             AVAILABLE → RESERVED
                        │
                     Payment
                        │
                  RESERVED → BOOKED
```

---

# 🎯 What you should be able to explain

After this lab, you should be able to answer:

> **Why not just use `synchronized`?**

Because it only coordinates threads inside one JVM.

> **Why not pessimistic locking everywhere?**

Because locks create waiting/contention and should only protect short critical sections.

> **Why not optimistic locking everywhere?**

Because under heavy contention, conflicts and retries can become expensive.

> **Why not SERIALIZABLE everywhere?**

Because stronger isolation can increase contention and reduce throughput.

> **Why reservation?**

Because temporary business ownership is different from a database lock.

> **Why Redis?**

For use cases such as fast temporary state, TTL, caching, and distributed coordination—not as a replacement for your source-of-truth database.

> **What's the simplest solution for a stock counter?**

Often an atomic conditional database update is worth considering before reaching for complicated locking.

---

## 🔥 Core takeaway

```text
Optimistic
→ "I'll detect conflicts."

Pessimistic
→ "I'll prevent conflicting writes while I hold the lock."

Atomic SQL
→ "I'll make this state transition one indivisible DB operation."

Reservation
→ "I'll temporarily give this resource to this user."

Redis TTL
→ "This temporary state should expire automatically."
```
