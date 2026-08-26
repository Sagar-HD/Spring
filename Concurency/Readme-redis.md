# ⚡ Redis — First-Principles README

> **Goal:** Understand Redis deeply enough to reason about caching, atomic operations, concurrency, distributed locks, and eventually build a Redis + PostgreSQL reservation system in Spring Boot.

---

## 1. What is Redis? 🧠

Redis (**Re**mote **Di**ctionary **S**erver) is an in-memory data store.

### Basic Architecture Comparison

```
Application ──► [Command] ──► Redis ──► Data in Memory (RAM)

```

Unlike traditional relational databases (like PostgreSQL) which are disk-oriented:

```
PostgreSQL ──► Disk-oriented Persistent Database ──► Tables ──► Rows ──► Indexes

```

Redis operates on key-value mappings where values are rich, native data structures:

```
Key
 │
 ├── 🔤 String
 ├── 🗂️ Hash
 ├── 📜 List
 ├── 🎲 Set
 ├── 🏆 Sorted Set (ZSET)
 └── 🌊 Stream

```

---

## 2. Why is Redis Fast? 🚀

1. **In-Memory Operations:** Keeps active datasets primarily in RAM, bypassing physical disk I/O bottlenecks.
2. **Optimized Data Structures:** Operations act directly on native memory-backed data types.

```
Application ──► Redis ──► RAM  (Microsecond Latency)
Application ──► Database ──► Disk (Millisecond Latency)

```

> ⚠️ **Key Rule:** Fast does not mean a total database replacement. Redis and PostgreSQL solve different problems.

### Dual-Database Pattern

```
                     Application
                    /           \
                   ▼             ▼
              ⚡ Redis        💾 PostgreSQL
                 │                 │
           Fast / Temporary      Durable
            State & Cache        Truth

```

---

## 3. Redis is Not Just a Cache 🧰

While caching is common, Redis handles many core distributed system roles:

* ⚡ **Caching** & **Session Storage**
* 🚦 **Rate Limiting** & **Counters**
* 📜 **Task Queues** & **Pub/Sub**
* 🔒 **Distributed Locks** & **Coordination**
* 🏆 **Leaderboards** & **Deduplication**
* 🌊 **Streams** & **Temporary State**

For a **Reservation System**, Redis provides:

1. **Atomic Operations**
2. **TTL (Time To Live)**
3. **Distributed Locks**
4. **Temporary Seat Reservations**

---

## 4. Redis Mental Model ⚙️

Redis works as a command-driven server:

```
Spring Boot ──► SET user:1 Sagar ──► Redis
                                       │
                                    user:1 ──► Sagar

```

Requesting data:

```
GET user:1  ──►  Returns: "Sagar"

```

---

## 5. Redis Strings 🔤

Strings are the foundational data type in Redis.

```bash
SET name Sagar
GET name        # Returns: "Sagar"

```

### Core String Commands

* `SET` / `GET` / `DEL` / `EXISTS`
* `INCR` / `DECR`
* `GETRANGE` / `SETRANGE`

### Batch Operations (MGET / MSET)

Instead of executing sequential single queries:

```bash
# Efficient batching
MGET user:1 user:2 user:3
MSET user:1 Sagar user:2 Rahul

```

---

## 6. SETNX (Set if Not Exists) 🔑

`NX` = **Set only if the key does not already exist.**

```bash
SET lock:seat:42 token-A NX

```

* **If key does NOT exist:** Returns `OK` (Key created)
* **If key ALREADY exists:** Returns `(nil)` (Operation ignored)

> 💡 `SETNX` gives Redis a powerful atomic primitive: *"Create this value only if nobody has created it yet."* This is the foundation for **Distributed Locks**.

---

## 7. TTL (Time To Live) & Expiration ⏳

Redis keys can expire automatically:

```bash
SET session:123 abc
EXPIRE session:123 60    # Expires in 60 seconds

```

### Modern Combined Syntax (`SET ... EX`)

```bash
SET session:123 abc EX 60

```

### Checking TTL

```bash
TTL session:123

```

* `positive integer` = Seconds remaining until expiration
* `-1` = Key exists, but has no TTL set
* `-2` = Key does not exist

### Why TTL Matters for Temporary State

```
reservation:seat:42 ──► TTL = 5 Minutes (300s)
                             │
            ┌────────────────┴────────────────┐
            ▼                                 ▼
   Payment Succeeded                 5 Minutes Elapsed (No Payment)
   ─────────────────                 ─────────────────────────────
   Persist to PostgreSQL             Key expires automatically
   Release lock                      Seat becomes available again

```

---

## 8. Atomic Operations & Race Conditions ⚔️

An operation is **atomic** when it happens as one indivisible step. No other command can interrupt or interleave.

```bash
DECR stock   # Atomically decrements stock from 10 to 9

```

### The Race Condition Bug (GET + Application Logic + SET)

```
   User A (Instance #1)                  User B (Instance #2)
           │                                     │
    1. GET stock (returns 1)              1. GET stock (returns 1)
           │                                     │
    2. Check: stock > 0? (True)           2. Check: stock > 0? (True)
           │                                     │
    3. SET stock 0                        3. SET stock 0
           ▼                                     ▼
   [ Both users believe they successfully claimed the last item! ]

```

> ❌ **Problem:** `GET` + `Application IF Check` + `SET` is **NOT** atomic.
> 💡 **Takeaway:** Individual atomic commands (like `DECR`, `INCR`, `SETNX`, `SADD`) do not automatically make an entire multi-step business operation atomic.

---

## 9. Data Structures Reference 📊

### 📜 Lists (Ordered Collections / Queues)

* **Commands:** `LPUSH`, `RPUSH`, `LPOP`, `RPOP`, `LLEN`, `LRANGE`, `BLPOP`
* **`LRANGE tasks 0 -1`**: Reads from index 0 to the end (`-1`).
* **`BLPOP queue 0`**: Blocking Left Pop. Waits until an item arrives, making it useful for task processing queues.

### 🎲 Sets (Unordered Unique Collections)

* **Commands:** `SADD`, `SREM`, `SMEMBERS`, `SISMEMBER`, `SCARD`, `SINTER`, `SUNION`, `SDIFF`
* **`SINTER userA:skills userB:skills`**: Finds common elements across sets (intersections).

### 🏆 Sorted Sets / ZSET (Score-Ordered Collections)

* **Structure:** Each member is paired with a numeric `Score`. Redis maintains ordering by score.
* **Commands:** `ZADD`, `ZRANGE`, `ZREVRANGE`, `ZRANK`, `ZREVRANK`, `ZSCORE`, `ZREM`
* **Use Cases:** Leaderboards, priority task scheduling, background job queues (e.g., BullMQ).

```bash
ZADD leaderboard 100 Sagar
ZADD leaderboard 200 Rahul
ZADD leaderboard 150 Amit

ZRANGE leaderboard 0 -1 WITHSCORES
# Output: Sagar (100), Amit (150), Rahul (200)

```

### 🗂️ Hashes (Objects / Maps)

* **Structure:** Key maps to multiple field-value pairs.
* **Commands:** `HSET`, `HGET`, `HGETALL`, `HDEL`, `HEXISTS`, `HKEYS`, `HVALS`

```bash
HSET user:1 name Sagar age 22 role developer
HGET user:1 name       # Returns "Sagar"
HGETALL user:1         # Returns all key-value pairs

```

### 🌊 Streams (Append-Only Event Logs)

* **Commands:** `XADD`, `XRANGE`, `XREAD`, `XGROUP`
* **Use Cases:** Message buses, event processing pipelines, audit activity tracking.

```bash
XADD events * type login user 42
XRANGE events - +

```

---

## 10. Redis Transactions (`MULTI` / `EXEC` / `WATCH`) 🔄

### Command Queuing with `MULTI` / `EXEC`

```bash
MULTI
SET a 10   # Returns QUEUED
SET b 20   # Returns QUEUED
INCR a     # Returns QUEUED
EXEC       # Executes queued commands consecutively

```

### Guarantees & Non-Rollback Behavior

1. **Sequential Execution:** Redis will execute all queued commands in order without interleaving requests from other clients.
2. **No Automatic Rollback:** If step 2 fails at runtime, step 1 is **NOT** rolled back. Redis transactions are about *atomic command sequence execution*, not SQL-style rollbacks.

```
PostgreSQL: BEGIN ──► Changes ──► Error ──► ROLLBACK (Undoes all changes)
Redis:      MULTI ──► Queue ──► EXEC ──► Run all sequentially (No undo on runtime error)

```

### Why `MULTI`/`EXEC` Cannot Express Conditional Application Logic

You **cannot** run a `GET`, read its value in application code, and decide what to queue inside an active `MULTI`/`EXEC` block because commands are queued without returning output until `EXEC` is called.

### Optimistic Locking with `WATCH`

`WATCH` monitors a key for changes before a transaction executes:

```
Client A                              Client B
─────────────────────────────────     ──────────────────────────
WATCH balance
GET balance (returns 100)
                                      SET balance 50  (Modifies key)
MULTI
SET balance 90
EXEC  ──► Returns (nil) [FAILED!]

```

> **WATCH Mental Model:** Optimistic concurrency control. If the watched key changes before `EXEC`, the transaction aborts automatically.

---

## 11. Lua Scripting 📜

Lua scripts execute custom conditional logic **directly inside the Redis engine atomically**.

```lua
-- Lua script executed inside Redis
local stock = tonumber(redis.call('GET', KEYS[1]))

if stock > 0 then
    redis.call('DECR', KEYS[1])
    return 1
else
    return 0
end

```

### `MULTI/EXEC` vs. Lua Scripts

| Dimension | `MULTI` / `EXEC` | Lua Script (`EVAL`) |
| --- | --- | --- |
| **Logic Type** | Fixed sequence of predetermined commands | Dynamic conditional logic (`if / else / loops`) |
| **Execution** | Commands queued & executed in order | Entire script runs as a single atomic operation |
| **Data Access** | Cannot inspect command output mid-transaction | Can read key values and branch execution dynamically |

---

## 12. Rate Limiting Pattern 🚦

Combining **Atomic Counters** + **TTL**:

```bash
INCR rate:user:42        # Increments counter
EXPIRE rate:user:42 60   # Sets window TTL (if first request)

```

If counter exceeds limit (e.g., `100` requests/min) ──► Reject Request.

When TTL expires ──► Key disappears, counter resets automatically.

---

## 13. Distributed Locks 🔒

When running multiple application instances (e.g., Spring Boot nodes behind a Load Balancer), standard JVM locks (`synchronized`, `ReentrantLock`) **do not work** across process boundaries.

```
                        Load Balancer
                       /      |      \
                      ▼       ▼       ▼
                  Spring #1 Spring #2 Spring #3
                      \       |       /
                       ▼      ▼      ▼
                        [ Redis ]

```

### Lock Primitive Syntax

```bash
SET lock:seat:42 unique-token-A NX EX 10

```

1. `lock:seat:42`: Target resource key.
2. `unique-token-A`: Owner token (UUID / random string).
3. `NX`: Only set if lock doesn't exist (Mutual Exclusion).
4. `EX 10`: Automatic expiry in 10 seconds (Prevents deadlocks on app crash).

### Why a Unique Owner Token is Mandatory

Without owner tokens, Node A might wake up from a slow garbage collection pause after its lock expired and accidentally delete a lock newly acquired by Node B:

```
Node A acquires lock (Token A, TTL 10s)
  │
[Node A pauses for 12s due to GC / DB latency]
  │
Lock expires automatically in Redis
  │
Node B acquires lock (Token B)
  │
Node A wakes up & runs "DEL lock:seat:42"  <-- BAD! Deletes Node B's active lock!

```

### Safe Lock Release with Lua

To safely release a lock, verify ownership and delete in **one atomic step**:

```lua
if redis.call("GET", KEYS[1]) == ARGV[1] then
    return redis.call("DEL", KEYS[1])
else
    return 0
end

```

### Distributed Lock Component Breakdown

| Mechanism | Problem Solved |
| --- | --- |
| **`NX` (Not Exists)** | Prevents multiple nodes from acquiring lock simultaneously |
| **`EX` (TTL)** | Prevents permanent lock holding if application node crashes |
| **`Unique Token`** | Identifies true lock owner before release |
| **`Lua Release Script`** | Ensures atomic *Check Token + Delete* to avoid race conditions |

---

## 14. Redis + PostgreSQL Reservation System 🏛️

```
Client Request (POST /reserve/42)
       │
       ▼
Acquire Redis Lock  ─── (Failed) ───► Reject / Retry
       │
       │ (Success)
       ▼
Check PostgreSQL DB
       │
   Is Seat Available?
    /            \
  (No)          (Yes)
   │              │
 Reject       Create Hold Record in DB
                  │
                  ▼
              Commit DB Tx
                  │
                  ▼
              Release Redis Lock

```

### Separation of Responsibilities

| System | Primary Role |
| --- | --- |
| **Redis** | Fast instance coordination, temporary lock acquisition, TTL holds |
| **PostgreSQL** | Durable source of truth, transactional invariants, permanent reservations |

> 🛡️ **Defense in Depth:** Even with Redis locking, PostgreSQL must maintain unique constraints on seat reservations (`seat_id` + `status`) to protect underlying business invariants.

### Lock vs. Database Transaction

* **Redis Lock:** Controls **WHO** enters the critical code section across distributed instances.
* **DB Transaction:** Controls **WHAT** data changes become committed atomically to storage.

---

## 15. Consistency, Failures & Idempotency ⚠️

Redis and PostgreSQL are independent systems. There is **no automatic global transaction** spanning both.

### Failure Modes & Handling

```
Scenario 1: Lock acquired ──► DB succeeds ──► App crashes before releasing lock
   Solution: Redis TTL automatically expires and cleans up lock key.

Scenario 2: Lock acquired ──► DB write fails ──► App catches exception & releases lock
   Solution: Ensure lock release logic runs inside a finally block / try-with-resources.

Scenario 3: DB succeeds ──► Response drops in network ──► Client retries request
   Solution: Enforce Idempotency Keys (e.g., store idempotency-key in Redis/DB).

```

---

## 16. Progression Map & Mental Model 🗺️

```
                        REDIS
                          │
                   What is Redis?
                          │
                  Data Architecture
                          │
        ┌─────────────────┼─────────────────┐
        ▼                 ▼                 ▼
     Strings         Collections         Streams
        │
     TTL / EXPIRE
        │
    Atomic Operations
        │
    MULTI / EXEC / WATCH
        │
    Lua Scripting
        │
    Distributed Locks (SET NX EX + Lua)
        │
    Redis + PostgreSQL Dual Architecture
        │
    Failure Modes & Distributed Consistency

```

### Summary

* **Do not treat Redis as:** *"A simple caching key-value store with GET and SET."*
* **Treat Redis as:** *"A high-performance in-memory data store with atomic data structures, capable of high-speed coordination and state locking across distributed application instances."*