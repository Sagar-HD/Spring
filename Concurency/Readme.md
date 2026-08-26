
# 📚 Database Transactions, Isolation Levels & Propagation — Spring Boot + MySQL

> A first-principles guide to understanding **transactions, isolation levels, concurrency anomalies, propagation, locking, and how these concepts affect real production systems**.

---

# 1. Why Do We Need Transactions?

A transaction groups multiple database operations into one logical unit.

Example:

```text
Book Ticket

1. Check ticket availability
2. Reserve ticket
3. Create booking
4. Update payment status
```

We want:

```text
ALL SUCCESS
    ↓
COMMIT
```

or:

```text
ANY FAILURE
    ↓
ROLLBACK
```

### Without a transaction

```text
Update seat       ✅
Create booking    ✅
Payment record    ❌

Database:
Seat updated
Booking created
Payment missing
```

This can leave inconsistent state.

### With a transaction

```text
BEGIN
  ↓
Update seat
  ↓
Create booking
  ↓
Payment record ❌
  ↓
ROLLBACK
```

Everything returns to the previous state.

---

# 2. ACID

Transactions provide the foundation for **ACID**.

```text
A → Atomicity
C → Consistency
I → Isolation
D → Durability
```

### Atomicity

All operations succeed or all roll back.

```text
A + B + C
    ↓
failure
    ↓
rollback A, B, C
```

### Consistency

The database moves from one valid state to another valid state.

Example:

```text
stock >= 0
```

should never become:

```text
stock = -1
```

### Isolation

Concurrent transactions should not improperly interfere with each other.

This is where isolation levels come in.

### Durability

Once committed:

```text
COMMIT
  ↓
data survives crash/restart
```


---

# 2.1 Atomic SQL Statement vs Transaction vs Isolation

These three ideas are easy to mix up.

## Every SQL statement is atomic

A single SQL statement is an atomic database operation.

For example:

```sql
UPDATE products
SET available_stock = available_stock - 1
WHERE id = ?
AND available_stock > 0;
```

The database does not leave this one statement half-applied.

Conceptually:

```text
ONE SQL STATEMENT
       ↓
   atomic operation
```

But **statement atomicity is not the same as transaction atomicity**.

## A transaction groups multiple SQL statements

Suppose creating an order requires:

```text
1. Reserve stock
2. Create order
3. Create payment record
4. Create order item
```

Now we need:

```text
BEGIN
   ↓
SQL #1
   ↓
SQL #2
   ↓
SQL #3
   ↓
SQL #4
   ↓
COMMIT
```

If SQL #3 fails:

```text
BEGIN
   ↓
SQL #1 ✅
   ↓
SQL #2 ✅
   ↓
SQL #3 ❌
   ↓
ROLLBACK
```

The earlier successful statements are rolled back.

So:

```text
SQL statement atomicity
→ one statement succeeds or fails as a unit

Transaction atomicity
→ multiple statements succeed together or roll back together
```

## Isolation is a different question

Once multiple transactions can run concurrently:

```text
T1 ───────────────
T2 ───────────────
```

we ask:

> What is T1 allowed to see from T2?

and:

> What happens if both modify related data?

That is **isolation**.

So the mental progression is:

```text
SQL statement
   ↓
atomic operation

Multiple SQL statements
   ↓
transaction boundary
   ↓
COMMIT / ROLLBACK

Multiple transactions running concurrently
   ↓
isolation + concurrency control
```

---

# 2.2 Complete Mental Model: Transaction, Isolation and Locking

This is the most important model in this README.

```text
                         DATABASE
                            │
              ┌─────────────┴─────────────┐
              ↓                           ↓
       SQL STATEMENTS                TRANSACTIONS
              │                           │
          atomic                      multiple SQLs
                                          │
                                  BEGIN → COMMIT
                                          │
                                   all succeed
                                          │
                                     OR ROLLBACK
                                          │
                                          ↓
                                    CONCURRENCY
                                          │
                                          ↓
                                      ISOLATION
                                          │
                              "What can this transaction
                               see from other transactions?"
                                          │
                     ┌────────────────────┼───────────────────┐
                     ↓                    ↓                   ↓
               READ COMMITTED      REPEATABLE READ      SERIALIZABLE
                     │                    │                   │
                     └────────────────────┴───────────────────┘
                                          │
                                          ↓
                              DATABASE CONCURRENCY CONTROL
                                          │
                     ┌────────────────────┼────────────────────┐
                     ↓                    ↓                    ↓
                  locks                 MVCC             conflict detection
```

The critical distinction:

```text
TRANSACTION
→ unit of work

ATOMICITY
→ all statements in that transaction succeed or roll back together

ISOLATION
→ rules for interaction/visibility between concurrent transactions

LOCKING
→ a mechanism used to coordinate access to particular data

SERIALIZABLE
→ an isolation guarantee for the whole transaction

OPTIMISTIC LOCKING
→ detect conflicting modification

PESSIMISTIC LOCKING
→ explicitly lock a resource while working on it

ATOMIC UPDATE
→ make a simple state transition indivisible
```

> **Transaction ≠ lock ≠ isolation ≠ reservation.**

---

# 2.3 Why `@Transactional` Does NOT Mean "Atomic SQL"

`@Transactional` creates a **transaction boundary** around multiple database operations.

It does not magically turn one method into one SQL statement.

```java
@Transactional
public void purchase() {
    reserveStock();       // SQL
    createOrder();        // SQL
    createPayment();      // SQL
}
```

Conceptually:

```text
BEGIN
   ↓
SQL #1
   ↓
SQL #2
   ↓
SQL #3
   ↓
COMMIT
```

The individual SQL statements were already atomic.

The transaction gives us **atomicity across the collection of statements**.

---

# 2.4 Where Isolation and Locking Fit

After creating a transaction, ask:

### Question 1 — How many SQL statements belong together?

If multiple:

```text
→ transaction
```

### Question 2 — What if one fails?

Need:

```text
→ COMMIT / ROLLBACK
```

### Question 3 — What if another transaction runs simultaneously?

Now we need:

```text
→ concurrency control
→ isolation
```

### Question 4 — What visibility do we need?

Choose an isolation level:

```text
READ UNCOMMITTED
READ COMMITTED
REPEATABLE READ
SERIALIZABLE
```

### Question 5 — Do we need explicit protection of a resource?

Potentially:

```text
Atomic UPDATE
Optimistic locking
Pessimistic locking
```

This decision process is much better than starting with:

> "Which lock should I use?"

---

# 2.5 Isolation Level vs Locking

These are related, but they are **not the same concept**.

## Isolation

Isolation answers:

> **What can a transaction see, and which concurrency anomalies are allowed?**

Examples:

```text
READ COMMITTED
REPEATABLE READ
SERIALIZABLE
```

## Locking

Locking answers:

> **How do we coordinate access to a particular resource?**

Examples:

```text
SELECT ... FOR UPDATE
@Version
PESSIMISTIC_WRITE
```

Mental model:

```text
Isolation
   ↓
rules / guarantees for concurrent transactions

Locking
   ↓
a mechanism for protecting particular data
```

Also important:

> An isolation level may internally use locks, MVCC, conflict detection, or a combination depending on the database.

So:

```text
SERIALIZABLE ≠ "lock everything"

PESSIMISTIC LOCK ≠ "an isolation level"
```

---

# 2.6 Optimistic Locking

Optimistic locking means:

> **Let concurrent work happen, but detect that somebody changed the row before accepting my stale update.**

Typical JPA:

```java
@Version
private Long version;
```

Example:

```text
Product
price   = ₹1000
version = 5
```

Two admins read it:

```text
Admin A → version 5
Admin B → version 5
```

A updates:

```text
price = ₹1200
version 5 → 6
```

B tries to save its stale version:

```text
expected version = 5
actual version   = 6
```

Result:

```text
OptimisticLockException
```

Mental model:

```text
OPTIMISTIC
    ↓
Don't block everyone
    ↓
Detect stale/conflicting modification
    ↓
Reject/retry the conflicting operation
```

### Good use cases

Optimistic locking fits well when:

- conflicts are relatively infrequent
- users edit records for some time before saving
- blocking another user while they edit would be undesirable
- retry/refresh is acceptable

### Good use cases in this project

```text
Admin edits product
Admin approves/rejects product
Admin edits vendor
User edits profile/address
Cart is modified concurrently
```

For example, two admins editing the same product is a much more natural optimistic-locking use case than stock reservation.

---

# 2.7 Pessimistic Locking

Pessimistic locking means:

> **I expect contention, so explicitly lock the resource while I perform the critical operation.**

Example:

```sql
SELECT *
FROM products
WHERE id = ?
FOR UPDATE;
```

Conceptually:

```text
Transaction A
     ↓
🔒 Product #10
     ↓
do short critical operation
     ↓
COMMIT
     ↓
🔓 unlock

Transaction B
     ↓
tries same row
     ↓
WAIT
```

Mental model:

```text
PESSIMISTIC
     ↓
"I expect conflict"
     ↓
lock now
     ↓
others wait
```

### Good use cases

Use it when:

- contention is expected
- the critical section is short
- the operation must inspect current state and then modify it
- waiting is preferable to discovering a conflict later

### Good project use case

For example:

```text
Admin manually adjusts inventory
```

You can deliberately lock the product:

```sql
SELECT *
FROM products
WHERE id = ?
FOR UPDATE;
```

Then perform the short inventory adjustment and commit.

This gives a real reason to practice pessimistic locking without using it unnecessarily for every operation.

---

# 2.8 Optimistic vs Pessimistic vs Atomic Update

These solve related but different problems.

| Technique | Mental model | Good fit |
|---|---|---|
| Atomic UPDATE | "Perform this state transition as one indivisible DB operation." | Stock decrease-if-available |
| Optimistic locking | "Let work happen; detect stale modification." | Admin editing/approval |
| Pessimistic locking | "Lock this resource while I work." | Highly contested short critical section |

For inventory:

```sql
UPDATE products
SET available_stock = available_stock - 1
WHERE id = ?
AND available_stock > 0;
```

is often cleaner than first reading stock and then deciding in application code.

---

# 2.9 SERIALIZABLE — What Does It Actually Guarantee?

`SERIALIZABLE` is the strongest standard isolation level.

Its guarantee is:

> **The committed result of concurrent transactions must be equivalent to some serial execution of those transactions.**

This does **not** necessarily mean:

```text
T1 physically completes
     ↓
ONLY THEN T2 starts
```

Instead:

```text
T1 ────────────────
T2 ────────────────
```

may physically overlap.

But the database must ensure the final result is equivalent to either:

```text
T1 completely → T2 completely
```

or:

```text
T2 completely → T1 completely
```

That is **logical serialization**.

---

# 2.10 Logical Serialization With Multiple Operations

A transaction can contain many SQL statements:

```text
T1:

BEGIN
 ↓
SQL #1
 ↓
SQL #2
 ↓
SQL #3
 ↓
SQL #4
 ↓
COMMIT
```

and:

```text
T2:

BEGIN
 ↓
SQL #1
 ↓
SQL #2
 ↓
SQL #3
 ↓
COMMIT
```

With `SERIALIZABLE`, their physical execution may overlap:

```text
T1: SQL1 ─── SQL2 ───────── SQL3 ─── COMMIT
             │
T2:      SQL1 ─── SQL2 ─── SQL3 ─── COMMIT
```

But the **committed result** must be explainable as if the whole transactions were ordered:

```text
T1 → T2
```

or:

```text
T2 → T1
```

It is the **whole transaction** that must be logically serializable, not merely one SQL statement.

---

# 2.11 How Does the Database Know Whether Execution Is Serializable?

The database does not simply let everything run blindly and check the final number afterward.

The exact implementation is database-specific.

A database's concurrency-control mechanism may use combinations of:

```text
locks
MVCC snapshots
read/write dependency tracking
conflict detection
range/predicate protection
transaction aborts
```

Conceptually, the database observes/controls transaction interactions.

Example:

```text
T1 reads X
T2 reads X

T1 writes X
T2 writes X
```

There are conflicting dependencies.

If those dependencies form a pattern that cannot correspond to any serial ordering, the database must prevent that outcome.

It can do this by:

```text
1. blocking a conflicting operation
2. aborting one transaction
3. detecting a serialization failure
4. using another internal concurrency-control mechanism
```

The implementation depends on the database engine.

For example, PostgreSQL uses Serializable Snapshot Isolation (SSI), while MySQL/InnoDB has its own locking/MVCC behavior.

So separate:

```text
WHAT WE REQUEST
→ SERIALIZABLE isolation

HOW DB IMPLEMENTS IT
→ locks / MVCC / dependency tracking / conflict detection / etc.

WHAT WE GET
→ result equivalent to a serial execution
```

---

# 2.12 Why SERIALIZABLE Is Not Just "Pessimistic Locking for the Whole Transaction"

This is a very common misconception.

### Pessimistic locking

```text
SELECT ... FOR UPDATE
```

means:

> **Explicitly lock this particular row/resource.**

### SERIALIZABLE

means:

> **The entire transaction must produce a result equivalent to some serial ordering.**

The DB might use locks internally to achieve that guarantee, but `SERIALIZABLE` is an **isolation guarantee**, not simply "lock every row touched by this transaction."

Mental model:

```text
Pessimistic locking
→ resource-level / explicitly requested protection

SERIALIZABLE
→ transaction-level isolation guarantee
```

---

# 2.13 Why Use Locks If SERIALIZABLE Exists?

Because stronger is not automatically better.

Suppose 1000 users perform unrelated operations:

```text
User A → Product 1
User B → Product 2
User C → Product 3
...
```

You usually want conflicts to be limited to the resources that actually conflict.

Targeted mechanisms can preserve more concurrency:

```text
Atomic UPDATE
→ protect one state transition

Optimistic lock
→ detect one row's stale update

Pessimistic lock
→ serialize access to one row
```

Using `SERIALIZABLE` for everything can cause:

```text
more contention
more waiting
more serialization failures
more retries
lower throughput
```

Therefore:

> **Use the smallest concurrency mechanism that correctly protects the business invariant.**

---

# 2.14 A Genuine SERIALIZABLE Use Case

Do not add `SERIALIZABLE` to the project just because it exists.

A useful example is a **capacity/registration feature**.

Suppose:

```text
Event
capacity = 100
```

A naive registration transaction does:

```text
BEGIN

1. SELECT COUNT(*) FROM registrations
2. Check count < capacity
3. INSERT registration

COMMIT
```

Two transactions can concurrently do:

```text
T1 → COUNT = 99
T2 → COUNT = 99
```

Both decide:

```text
99 < 100
```

Both insert.

The invariant can be violated.

This is a multi-operation, predicate-based business rule.

`SERIALIZABLE` can be used to force the concurrent transactions to behave as if the capacity checks occurred serially.

However, a production system might choose a more targeted solution such as:

```text
atomic counter
row locking on the event
database constraint/design
```

So the point is to understand **why SERIALIZABLE exists**, not to use it everywhere.

---

# 2.15 SERIALIZABLE vs Atomic Inventory Update

Your main stock operation is:

```sql
UPDATE products
SET available_stock = available_stock - 1
WHERE id = ?
AND available_stock > 0;
```

This already expresses the invariant:

> **Decrease stock only if enough stock exists.**

The database performs that state transition atomically.

Therefore you do **not** need:

```text
whole order transaction = SERIALIZABLE
```

just to protect this one invariant.

For your project:

```text
Stock reservation
        ↓
Atomic conditional UPDATE
```

is the preferred implementation.

---

# 2.16 SERIALIZABLE vs Payment State Transition

The payment-vs-expiry race also does not require making the entire transaction `SERIALIZABLE`.

Use conditional state transitions:

Payment:

```sql
UPDATE orders
SET status = 'CONFIRMED'
WHERE id = ?
AND status = 'PAYMENT_PENDING';
```

Expiry:

```sql
UPDATE orders
SET status = 'EXPIRED'
WHERE id = ?
AND status = 'PAYMENT_PENDING'
AND expires_at < NOW();
```

Whichever successfully changes the row wins.

```text
PAYMENT_PENDING
      │
 ┌────┴────┐
 ↓         ↓
SUCCESS   EXPIRED
```

The other operation sees:

```text
affectedRows = 0
```

This is targeted concurrency control.

---

# 2.17 Deadlocks

Deadlock means transactions are waiting for each other in a cycle.

Classic pessimistic-lock example:

```text
T1:
lock Row A
   ↓
wait for Row B

T2:
lock Row B
   ↓
wait for Row A
```

Result:

```text
T1 → waits for T2
T2 → waits for T1

        🔄
     DEADLOCK
```

The database detects the deadlock and normally aborts one transaction.

## Optimistic locking

The optimistic-locking mechanism itself normally produces:

```text
version conflict
```

rather than the classic:

```text
T1 waits for T2
T2 waits for T1
```

However, the underlying database can still use locks for actual writes, indexes, foreign keys, etc.

Therefore:

> **Optimistic locking does not guarantee that absolutely no database deadlock can ever occur.**

## SERIALIZABLE

Depending on the database implementation, serializable transactions can encounter:

```text
classic lock deadlocks
```

or:

```text
serialization failures
```

A serialization failure is not necessarily the same thing as a deadlock.

For example, an MVCC/SSI-based implementation may detect a dangerous dependency pattern and abort one transaction:

```text
T1 → COMMIT
T2 → serialization failure
```

So:

| Technique | Typical conflict |
|---|---|
| Optimistic locking | Version conflict |
| Pessimistic locking | Lock wait / possible deadlock |
| SERIALIZABLE | Serialization failure and/or lock deadlock depending on DB implementation |

---

# 2.18 Never Hold a Database Lock During Payment

Bad:

```text
BEGIN
 ↓
LOCK inventory row
 ↓
CALL Stripe
 ↓
wait for network
 ↓
payment
 ↓
COMMIT
 ↓
unlock
```

Now other requests may wait for the duration of an external network call.

Better:

```text
AVAILABLE
   ↓
short DB transaction
   ↓
RESERVED
   ↓
COMMIT
   ↓
Stripe payment
   ↓
short DB transaction
   ↓
CONFIRMED
```

Or:

```text
RESERVED
   ↓
expiresAt reached
   ↓
EXPIRED
   ↓
release inventory
```

The database lock, if one is needed, should normally protect only the short critical state transition—not the external payment operation.

---

# 2.19 Your Project's Concurrency Strategy

For the normal e-commerce version:

```text
                         CONCURRENCY
                              │
             ┌────────────────┼────────────────┐
             ↓                ↓                ↓
        Stock update      Admin editing    Admin operation
             │                │                │
             ↓                ↓                ↓
      Atomic UPDATE      Optimistic lock   Pessimistic lock
                              │                │
                         @Version          FOR UPDATE
```

### Stock

```sql
UPDATE products
SET available_stock = available_stock - ?
WHERE id = ?
AND available_stock >= ?;
```

Use atomic DB operation.

### Admin product/vendor editing

```java
@Version
private Long version;
```

Use optimistic locking.

### Manual/high-contention short critical operation

```sql
SELECT ...
FOR UPDATE;
```

Use pessimistic locking.

### Complex multi-row/predicate business rule

Potentially:

```text
SERIALIZABLE
```

but only when the business invariant actually requires transaction-wide serializability.

---

# 2.20 Complete Concurrency Decision Tree

When you face a concurrency problem:

```text
START
  │
  ↓
Can the invariant be expressed as ONE atomic SQL operation?
  │
 ┌┴──────────────┐
YES              NO
 │                │
 ↓                ↓
Atomic SQL       Multiple operations depend
                 on concurrent state
                       │
                       ↓
                 What kind of conflict?
                  │             │
                  ↓             ↓
            Stale editing   High contention /
            is acceptable   need current row
                  │             │
                  ↓             ↓
             Optimistic    Pessimistic
               locking       locking
                  │             │
                  └──────┬──────┘
                         ↓
               Does correctness depend on
               the combined state of multiple
               reads/writes/predicates such that
               the whole transaction must behave
               serially?
                         │
                    YES  │  NO
                         ↓
                    SERIALIZABLE
```

This is a decision framework—not a rule that every problem must use one of these.

---

# 2.21 Spring Transaction Thread Context

Spring transactions are associated with the current execution/thread context.

```text
Thread A
   ↓
Transaction A

Thread B
   ↓
Transaction B
```

A manually-created thread does not automatically inherit the caller's transaction.

```java
@Transactional
public void method() {

    new Thread(() -> {
        // ❌ does not automatically inherit
        // the caller's transaction
    }).start();
}
```

For concurrency experiments:

```text
main/test thread
   ↓
creates multiple worker threads
   ↓
each worker calls a transactional Spring method
   ↓
each gets its own transaction
```

This is important when testing:

- optimistic locking
- pessimistic locking
- lost updates
- isolation anomalies
- serialization failures

---

# 2.22 Transaction Propagation vs Isolation

Do not confuse these.

### Isolation

Answers:

> **How does this transaction interact with concurrent transactions?**

```text
READ COMMITTED
REPEATABLE READ
SERIALIZABLE
```

### Propagation

Answers:

> **What should Spring do when a transactional method calls another transactional method?**

```text
REQUIRED
REQUIRES_NEW
MANDATORY
SUPPORTS
NOT_SUPPORTED
NEVER
NESTED
```

Example:

```java
@Transactional(
    isolation = Isolation.REPEATABLE_READ,
    propagation = Propagation.REQUIRED
)
public void bookTicket() {
}
```

These two attributes solve completely different problems.

---

# 2.23 Flash Sale: Why We Don't Start With Redis

For the normal version:

```text
Request
   ↓
DB
   ↓
Atomic stock UPDATE
```

This solves correctness.

Later, when a flash sale creates:

```text
10,000 requests
       ↓
same product
       ↓
same inventory row
       ↓
very high contention
```

the DB atomic operation can still be **correct**, but the database row can become extremely hot.

Then Redis can be introduced as a high-speed inventory gate:

```text
10,000 requests
       ↓
Redis
       ↓
only available reservations
       ↓
DB/payment processing
```

Redis reservation and Redis distributed locking are different concepts.

### Redis reservation

Answers:

> "Is there inventory available for this request?"

### Redis distributed lock

Answers:

> "Which application instance is allowed to execute this distributed critical section?"

Do not introduce Redis locking merely because Redis is being used.

---

# 2.24 Reservation Is Not the Same as a Database Lock

This distinction matters for payment systems.

A business reservation:

```text
AVAILABLE
   ↓
RESERVED
   ↓
PAYMENT
   ↓
CONFIRMED
```

is not the same thing as:

```text
DB row lock
```

A reservation can last for minutes.

A database lock should generally last only for the short database transaction.

Never do:

```text
DB lock
   ↓
Stripe
   ↓
5 minute payment window
```

Instead:

```text
short DB transaction
   ↓
reservation state
   ↓
COMMIT
   ↓
external payment
```

---

# 2.25 Payment + Reservation Concurrency

For the normal system:

```text
POST /orders
      ↓
short DB transaction
      ↓
atomic stock reservation
      ↓
create PAYMENT_PENDING order
      ↓
COMMIT
      ↓
Stripe Checkout
```

If the user pays:

```text
Stripe
   ↓
webhook
   ↓
conditional state transition
   ↓
CONFIRMED
```

If the user disappears:

```text
expiry scheduler
   ↓
conditional state transition
   ↓
EXPIRED
   ↓
release stock
```

The scheduler and webhook can arrive almost simultaneously:

```text
             PAYMENT_PENDING
                    │
           ┌────────┴────────┐
           ↓                 ↓
      Stripe webhook     Expiry job
           ↓                 ↓
       CONFIRMED           EXPIRED
```

Only one should successfully transition the order from `PAYMENT_PENDING`.

---

# 2.26 Late Payment After Expiry

Suppose:

```text
PAYMENT_PENDING
      ↓
EXPIRED
      ↓
stock released
      ↓
later Stripe webhook says SUCCESS
```

Do **not** resurrect the order.

The product may already have been sold to somebody else.

Instead:

```text
Order  = EXPIRED
Payment = SUCCESS
       ↓
REFUND_PENDING
       ↓
REFUNDED
```

This is a real-world asynchronous-system edge case.

---

# 2.27 Webhook, Refund and Reconciliation

### Webhook

Primary real-time payment notification:

```text
Stripe
   ↓
Webhook
   ↓
Backend
```

### Refund handling

If money succeeded but the order cannot be fulfilled:

```text
SUCCESS
   ↓
REFUND_PENDING
   ↓
REFUNDED
```

### Reconciliation

A periodic safety net.

If:

```text
Your DB:
PAYMENT_PENDING

Stripe:
SUCCESS
```

because a webhook was delayed, failed, or the application was unavailable:

```text
reconciliation job
       ↓
ask Stripe for actual status
       ↓
repair local state
```

Mental model:

```text
Webhook
→ real-time notification

Reconciliation
→ periodic audit/recovery
```

---

# 2.28 Final Mental Model

If you remember only one diagram from this README, remember this:

```text
                         DATABASE
                            │
                            ↓
                       SQL STATEMENT
                            │
                         atomic
                            │
                            ↓
                     MULTIPLE SQLs
                            │
                            ↓
                       TRANSACTION
                    BEGIN → COMMIT
                       │
                  failure → ROLLBACK
                       │
                       ↓
                   CONCURRENCY
                       │
                       ↓
                    ISOLATION
                       │
          ┌────────────┼────────────┐
          ↓            ↓            ↓
    READ COMMITTED  REPEATABLE   SERIALIZABLE
                    READ
                       │
                       ↓
              concurrency control
                       │
       ┌───────────────┼────────────────┐
       ↓               ↓                ↓
  Atomic UPDATE   Optimistic       Pessimistic
                  @Version         FOR UPDATE
       │               │                │
  simple state     stale update     current row +
  transition       detection       exclusive access
                       │
                       └────────┬───────┘
                                ↓
                     targeted concurrency
                                │
                                ↓
                    SERIALIZABLE when the
                    WHOLE transaction's
                    correctness requires
                    serializable behavior
```

And the final production mental model:

```text
Transaction
→ unit of work

Atomicity
→ all operations in the transaction succeed or roll back

Isolation
→ how concurrent transactions interact

Propagation
→ how Spring transaction boundaries behave across method calls

Atomic UPDATE
→ protect a simple state transition

Optimistic locking
→ detect stale modification

Pessimistic locking
→ explicitly serialize access to a resource

SERIALIZABLE
→ whole transaction's committed result must be equivalent
  to some serial execution

Reservation
→ temporary business state, NOT a DB lock

Idempotency
→ repeated delivery/request should produce one intended effect
```

---

# 2.29 Production Rule of Thumb

Do not start with:

> "Which lock should I use?"

Start with:

```text
1. What business invariant am I protecting?
2. How many SQL operations are involved?
3. Can one atomic SQL statement express the invariant?
4. If not, is this a stale-update problem?
5. If not, do I need exclusive current-row access?
6. Does correctness depend on the combined state of multiple
   reads/writes/predicates across the whole transaction?
7. Only then consider SERIALIZABLE.
```

The goal is not maximum isolation.

The goal is:

> **The smallest concurrency-control mechanism that guarantees the business invariant while preserving as much useful concurrency as possible.**

---

# 3. Spring `@Transactional`

Spring provides declarative transaction management:

```java
@Transactional
public void bookTicket() {
    // database operations
}
```

Spring creates a transaction around the method.

Conceptually:

```text
method starts
     ↓
BEGIN
     ↓
database operations
     ↓
method succeeds
     ↓
COMMIT
```

If an appropriate exception causes rollback:

```text
method fails
     ↓
ROLLBACK
```

---

# 4. Transaction Boundary

The location of `@Transactional` matters.

Usually:

```text
Controller
    ↓
Service
    ↓
@Transactional
    ↓
Repository
    ↓
Database
```

The **service layer** is generally a natural transaction boundary because a business operation may involve multiple repository calls.

Example:

```java
@Transactional
public void purchaseProduct() {

    Product product = productRepository.findById(id);

    product.decreaseStock();

    Order order = new Order(...);

    orderRepository.save(order);
}
```

Both operations belong to the same transaction.

---

# 5. Transactions Are Thread-Bound

This is extremely important.

Spring transaction context is associated with the current thread.

```text
Thread A
   ↓
Transaction A

Thread B
   ↓
Transaction B
```

A transaction does **not automatically jump to a manually-created thread**.

Therefore:

```java
@Transactional
public void method() {

    new Thread(() -> {
        // ❌ does not automatically inherit this transaction
    }).start();
}
```

This is why your concurrency experiments need each thread to enter its own transactional Spring method.

---

# 6. Isolation

Imagine:

```text
Transaction A
Transaction B
```

Both operate concurrently.

The database must decide:

> What is A allowed to see from B?

and:

> What happens when A and B modify the same data?

That's what **transaction isolation** controls.

---

# 7. Isolation Levels

The standard levels are:

```text
READ UNCOMMITTED
READ COMMITTED
REPEATABLE READ
SERIALIZABLE
```

Think of them as increasing isolation:

```text
Weakest
   ↓
READ UNCOMMITTED
   ↓
READ COMMITTED
   ↓
REPEATABLE READ
   ↓
SERIALIZABLE
Strongest
```

Generally:

```text
More isolation
      ↓
More consistency guarantees
      ↓
Potentially more contention / less concurrency
```

But exact behavior depends on the database engine.

For this README:

> **Database = MySQL + InnoDB**

---

# 8. READ UNCOMMITTED

The transaction can potentially see **uncommitted changes** from another transaction.

Example:

Initial:

```text
availableSeats = 2
```

Transaction A:

```text
BEGIN

UPDATE seats
SET availableSeats = 1;

-- not committed yet
```

Transaction B:

```text
BEGIN

SELECT availableSeats;
```

B may see:

```text
1
```

even though A hasn't committed.

Then:

```text
A → ROLLBACK
```

Actual database value:

```text
2
```

But B already observed:

```text
1
```

This is a:

# Dirty Read

```text
T1
UPDATE → 1
   │
   │ uncommitted
   ↓
T2
READ → 1 ❌
   │
T1 ROLLBACK
   ↓
DB = 2
```

The problem:

> Transaction B read data that never actually became permanent.

### Spring

```java
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public void operation() {
}
```

### When would you use it?

Almost never for critical business data.

---

# 9. READ COMMITTED

A transaction can only see **committed data**.

Example:

Initial:

```text
availableSeats = 2
```

Transaction A:

```text
BEGIN

SELECT availableSeats;
→ 2
```

Transaction B:

```text
UPDATE availableSeats = 1;
COMMIT;
```

Transaction A reads again:

```text
SELECT availableSeats;
→ 1
```

Same transaction saw:

```text
2 → 1
```

This is:

# Non-Repeatable Read

```text
T1
READ → 2
      │
      │
T2   UPDATE → 1
      │
      COMMIT
      │
T1
READ → 1
```

The first read was committed data.

The second read was also committed data.

But the value changed between reads.

### Spring

```java
@Transactional(isolation = Isolation.READ_COMMITTED)
public void operation() {
}
```

### Why is this often acceptable?

For many normal APIs, you don't need the entire transaction to see one frozen snapshot.

Example:

```text
GET dashboard
```

You generally care about reasonably current committed data.

---

# 10. REPEATABLE READ

MySQL/InnoDB's default isolation level is:

```text
REPEATABLE READ
```

The important idea:

> Normal reads within the transaction operate against a consistent snapshot.

Example:

Initial:

```text
availableSeats = 2
```

T1:

```text
BEGIN

SELECT availableSeats;
→ 2
```

T2:

```text
UPDATE availableSeats = 1;
COMMIT;
```

T1:

```text
SELECT availableSeats;
→ 2
```

T1 continues seeing its consistent snapshot.

```text
T1:

READ → 2
       ↓
T2 changes DB → 1
       ↓
READ → 2
```

This prevents the non-repeatable-read behavior seen under READ COMMITTED for normal consistent reads.

### Spring

```java
@Transactional(isolation = Isolation.REPEATABLE_READ)
public void operation() {
}
```

---

# 11. The Trade-Off of REPEATABLE READ

REPEATABLE READ is not simply:

> "Better READ COMMITTED."

It gives you a stronger consistency guarantee, but the snapshot can become **stale relative to the current database state**.

Example:

```text
T1 snapshot
stock = 2

T2 commits
stock = 1

T1 normal SELECT
stock = 2
```

The database's current committed state is:

```text
1
```

but T1's consistent snapshot says:

```text
2
```

This can matter when business logic needs the **latest current state**.

That's where a locking/current read may be appropriate:

```sql
SELECT *
FROM seats
WHERE id = ?
FOR UPDATE;
```

Now you're not simply asking:

> "What did my transaction's snapshot contain?"

You're asking for the current row while acquiring a lock.

---

# 12. REPEATABLE READ ≠ Row Lock

This distinction is critical.

Normal:

```sql
SELECT *
FROM seats
WHERE id = 1;
```

is a consistent read.

Whereas:

```sql
SELECT *
FROM seats
WHERE id = 1
FOR UPDATE;
```

is a **locking read**.

Conceptually:

```text
Normal SELECT
    ↓
consistent snapshot

SELECT FOR UPDATE
    ↓
current/latest row
    +
row lock
```

So:

> **Isolation level and explicit locking are different concepts.**

---

# 13. Lost Update

This is one of the most important concurrency problems for your Flash Sale.

Initial:

```text
stock = 2
```

Two transactions:

```text
T1 → READ stock = 2
T2 → READ stock = 2
```

Both calculate:

```text
2 - 1 = 1
```

T1:

```text
UPDATE stock = 1
COMMIT
```

T2:

```text
UPDATE stock = 1
COMMIT
```

Final:

```text
stock = 1
```

But two purchases happened.

Expected:

```text
stock = 0
```

The second update effectively overwrote the first transaction's result.

This is a:

# Lost Update

---

# 14. How to Solve Lost Updates

There are several strategies.

### Optimistic locking

```java
@Version
private Long version;
```

Hibernate detects that another transaction modified the entity.

---

### Pessimistic locking

```sql
SELECT *
FROM products
WHERE id = ?
FOR UPDATE;
```

One transaction locks the row while modifying it.

---

### Atomic update

Often the cleanest solution for simple inventory:

```sql
UPDATE products
SET available_stock = available_stock - 1
WHERE id = ?
AND available_stock > 0;
```

Then:

```text
affectedRows = 1
→ success

affectedRows = 0
→ unavailable
```

The database performs the state transition atomically.

---

# 15. Phantom Read 👻

A phantom read is different from a non-repeatable read.

### Non-repeatable read

An **existing row changes**.

```text
T1 → row A = ₹100

T2 → changes row A = ₹200

T1 → reads row A = ₹200
```

### Phantom read

The **set of rows matching a query changes**.

Initial:

```text
Available seats:

A1
A2
```

T1:

```sql
SELECT *
FROM seats
WHERE status = 'AVAILABLE';
```

Result:

```text
A1
A2
```

T2:

```text
INSERT A3
status = AVAILABLE;

COMMIT;
```

T1 runs the query again.

It may now get:

```text
A1
A2
A3
```

A new matching row appeared.

That's the **phantom**.

```text
First query
→ [A1, A2]

T2 inserts A3

Second query
→ [A1, A2, A3]
```

---

# 16. Why Phantom Reads Matter

Imagine:

```sql
SELECT COUNT(*)
FROM seats
WHERE event_id = 10
AND status = 'AVAILABLE';
```

First result:

```text
100
```

Another transaction creates/releases matching rows.

Second result:

```text
101
```

The query's **result set changed**, not merely the contents of an existing row.

---

# 17. SERIALIZABLE

The strongest standard isolation level.

The goal:

> Concurrent transactions must produce a result equivalent to some serial execution.

Conceptually:

```text
Without SERIALIZABLE

T1 ───────────┐
              ├── concurrent
T2 ───────────┘
```

With serializable guarantees:

```text
Valid outcome must be equivalent to:

T1 → T2

or

T2 → T1
```

The database may therefore:

```text
wait
```

or:

```text
abort a transaction
```

depending on the conflict and database implementation.

### Important

SERIALIZABLE does **not** mean:

```text
Java synchronized
```

and it doesn't necessarily mean:

> "Only one transaction can ever execute."

It means the **result must satisfy serializability**.

### Spring

```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public void operation() {
}
```

---

# 18. Isolation Trade-Off

Think:

```text
READ UNCOMMITTED
        ↓
more concurrency
less protection

READ COMMITTED
        ↓
reasonable default for many systems

REPEATABLE READ
        ↓
stronger snapshot consistency

SERIALIZABLE
        ↓
strongest isolation
potentially more waiting/conflicts
```

There is no universal:

> "Always use SERIALIZABLE."

Higher isolation can reduce concurrency and increase contention.

---

# 19. Isolation vs Locking

These are related but **not the same thing**.

### Isolation

Controls:

> **What can a transaction see and what concurrency anomalies are allowed?**

Examples:

```text
READ COMMITTED
REPEATABLE READ
SERIALIZABLE
```

### Locking

Controls:

> **How concurrent transactions are prevented from modifying/using a particular resource simultaneously.**

Examples:

```text
@Version
SELECT FOR UPDATE
PESSIMISTIC_WRITE
```

Think:

```text
Isolation
    ↓
rules for transaction visibility/concurrency

Locking
    ↓
mechanism for protecting particular data
```

---

# 20. Optimistic vs Pessimistic

### Optimistic

```java
@Version
private Long version;
```

Mental model:

> "I'll allow concurrent work and detect conflicts."

Good when:

* conflicts aren't extremely frequent
* retry is acceptable
* you don't want long lock waits

---

### Pessimistic

```sql
SELECT ...
FOR UPDATE;
```

Mental model:

> "I expect contention, so I'll lock the resource."

Good when:

* resource is highly contested
* operation must be serialized
* critical section is short

---

# 21. Never Hold a DB Lock During Payment

Bad:

```text
BEGIN
 ↓
LOCK seat
 ↓
CALL payment gateway
 ↓
wait 5 seconds
 ↓
payment
 ↓
COMMIT
```

Now everyone waits behind the payment operation.

Instead:

```text
AVAILABLE
    ↓
RESERVED
    ↓
payment
    ↓
CONFIRMED
```

The reservation can have:

```text
expiresAt
```

and Redis can provide:

```text
TTL
```

The DB lock should generally protect only the **short state transition**, not the entire payment process.

---

# 22. Transaction Propagation

Isolation answers:

> **How does this transaction interact with other transactions?**

Propagation answers:

> **What should Spring do when a transactional method calls another transactional method?**

---

# 23. `REQUIRED`

Default.

```java
@Transactional
```

means:

```text
If transaction exists
    ↓
join it

If no transaction
    ↓
create one
```

Example:

```text
Service A
@Transactional
    ↓
Service B
@Transactional(REQUIRED)
```

B joins A's transaction.

```text
Transaction A
   │
   ├── Service A
   │
   └── Service B
```

This is what you normally want.

---

# 24. `REQUIRES_NEW`

Always creates a new transaction.

The existing transaction is suspended.

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
```

Flow:

```text
Transaction A
     ↓
Service B
     ↓
suspend A
     ↓
Transaction B
     ↓
commit B
     ↓
resume A
```

### Real use case

Audit/logging that must commit independently.

```text
Main transaction
    ↓
payment operation
    ↓
failure → rollback

Audit transaction
    ↓
"Payment attempt failed"
    ↓
COMMIT
```

Be careful: excessive `REQUIRES_NEW` can increase connection usage and complexity.

---

# 25. `MANDATORY`

Requires an existing transaction.

```java
@Transactional(propagation = Propagation.MANDATORY)
```

If no transaction exists:

```text
❌ exception
```

Useful when a method **must never execute outside an existing transaction**.

This is what caused the exception you encountered during your thread experiment.

---

# 26. `SUPPORTS`

```java
@Transactional(propagation = Propagation.SUPPORTS)
```

If transaction exists:

```text
→ join it
```

If not:

```text
→ execute without transaction
```

Useful when a method can work either way.

---

# 27. `NOT_SUPPORTED`

```java
@Transactional(propagation = Propagation.NOT_SUPPORTED)
```

If a transaction exists:

```text
→ suspend it
```

Then execute without a transaction.

Rare, but useful when an operation should explicitly avoid participating in a surrounding transaction.

---

# 28. `NEVER`

```java
@Transactional(propagation = Propagation.NEVER)
```

The method must **not** execute inside a transaction.

If a transaction exists:

```text
❌ exception
```

Rare.

---

# 29. `NESTED`

```java
@Transactional(propagation = Propagation.NESTED)
```

Conceptually uses a **savepoint** within an existing transaction when supported by the transaction manager/database.

Example:

```text
Transaction A
   ↓
Operation 1
   ↓
SAVEPOINT
   ↓
Nested operation fails
   ↓
rollback to savepoint
   ↓
Operation 1 can continue
   ↓
COMMIT
```

This is different from `REQUIRES_NEW`.

### `REQUIRES_NEW`

```text
A suspended
B independent
```

### `NESTED`

```text
A
 ├── operation 1
 ├── savepoint
 └── nested operation
```

---

# 30. Propagation Cheat Sheet

| Propagation     | Existing TX? | Behavior         |
| --------------- | ------------ | ---------------- |
| `REQUIRED`      | Yes          | Join             |
| `REQUIRED`      | No           | Create           |
| `REQUIRES_NEW`  | Yes          | Suspend + new    |
| `REQUIRES_NEW`  | No           | Create new       |
| `MANDATORY`     | Yes          | Join             |
| `MANDATORY`     | No           | Exception        |
| `SUPPORTS`      | Yes          | Join             |
| `SUPPORTS`      | No           | No transaction   |
| `NOT_SUPPORTED` | Yes          | Suspend          |
| `NOT_SUPPORTED` | No           | No transaction   |
| `NEVER`         | Yes          | Exception        |
| `NEVER`         | No           | No transaction   |
| `NESTED`        | Yes          | Savepoint        |
| `NESTED`        | No           | New transaction* |

*Exact behavior depends on transaction manager/database support.

---

# 31. Isolation + Propagation Together

They solve different problems.

```java
@Transactional(
    isolation = Isolation.REPEATABLE_READ,
    propagation = Propagation.REQUIRED
)
public void bookTicket() {
}
```

### `isolation`

Controls:

```text
How this transaction interacts with
concurrent transactions.
```

### `propagation`

Controls:

```text
What happens when this method
is called while another transaction exists.
```

---

# 32. A Real Example

Suppose:

```text
POST /events/10/book
```

Controller:

```text
HTTP
 ↓
Controller
 ↓
BookingService
```

Booking service:

```java
@Transactional(
    isolation = Isolation.REPEATABLE_READ,
    propagation = Propagation.REQUIRED
)
public void book() {
}
```

Inside:

```text
1. Check availability
2. Reserve seat
3. Create booking
4. Update inventory
```

All belong to the same transaction.

---

# 32.1 Locking Strategy for This Project

For the normal e-commerce implementation, do **not** force every concurrency mechanism into the same flow.

Use each where it naturally fits:

```text
Stock reservation
    ↓
Atomic conditional UPDATE
```

```text
Admin product/vendor editing
    ↓
Optimistic locking (@Version)
```

```text
Short, highly-contested admin inventory operation
    ↓
Pessimistic locking (FOR UPDATE)
```

```text
Complex multi-row/predicate business rule
    ↓
Potential SERIALIZABLE experiment
```

A useful practice feature for understanding `SERIALIZABLE` is event registration:

```text
Event capacity = 100

Transaction:
1. COUNT registrations
2. Check count < capacity
3. INSERT registration
```

Concurrent transactions can both pass the check unless the design provides stronger coordination.

This is a good lab for comparing:

```text
READ COMMITTED
vs
SERIALIZABLE
```

But the production solution may ultimately use a more targeted mechanism such as an atomic counter, row lock, or database constraint.

# 33. Flash Sale Mental Model

Your Flash Sale might eventually have:

```text
10,000 requests
       ↓
Inventory
       ↓
concurrency control
       ↓
reservation
       ↓
payment
       ↓
confirmation
```

Don't use one giant transaction for everything.

Instead:

```text
Short DB transaction
       ↓
Reserve inventory
       ↓
COMMIT
       ↓
Payment
       ↓
Short DB transaction
       ↓
Confirm reservation
```

If reservation expires:

```text
RESERVED
   ↓
expiresAt reached
   ↓
EXPIRED
   ↓
inventory released
```

This is fundamentally different from holding a database lock during payment.

---

# 34. The Big Picture

```text
                         TRANSACTION
                              │
              ┌───────────────┴────────────────┐
              ↓                                ↓
          ISOLATION                       PROPAGATION
              │                                │
     "How do I interact              "What happens when
      with other TXs?"                 another TX exists?"
              │                                │
       ┌──────┼───────┐                  ┌─────┼──────┐
       ↓      ↓       ↓                  ↓     ↓      ↓
      RU      RC      RR             REQUIRED  NEW  MANDATORY
                       │
                       ↓
                  SERIALIZABLE
```

And separately:

```text
             CONCURRENCY CONTROL
                     │
        ┌────────────┼─────────────┐
        ↓            ↓             ↓
   Optimistic    Pessimistic    Atomic SQL
    @Version     FOR UPDATE     UPDATE WHERE
```

And for long-running business processes:

```text
             RESERVATION
                  │
        AVAILABLE → RESERVED
                  │
             Payment
             /       \
            ↓         ↓
       CONFIRMED    EXPIRED
```

---

# 🎯 What you actually need to remember

Don't memorize 20 annotations.

Your core mental model should be:

```text
TRANSACTION
→ unit of work

ISOLATION
→ what concurrent transactions can see

PROPAGATION
→ how transaction boundaries behave across method calls

OPTIMISTIC LOCK
→ detect conflicting modification

PESSIMISTIC LOCK
→ lock resource while transaction operates

ATOMIC UPDATE
→ make simple state transition indivisible

RESERVATION
→ temporary business ownership

IDEMPOTENCY
→ repeated request produces one intended effect
```

And the most important production lesson:

> **Transaction ≠ lock ≠ isolation ≠ reservation.**

They solve related but different problems. Understanding that distinction is what will let you make good decisions in your Flash Sale and, later, your multi-tenant event-booking system.

