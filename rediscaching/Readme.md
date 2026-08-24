# 🚀 Redis & Caching with Spring Boot

A hands-on exploration of **Redis caching with Spring Boot**, starting from manual `RedisTemplate` operations and progressing to Spring's annotation-based caching abstraction.

The goal was not just to use `@Cacheable`, but to understand **what caching is doing underneath the abstraction**.

---

## 🧠 1. What is Caching?

Caching means storing frequently accessed data in a **faster storage layer** so that expensive operations don't have to be repeated.

### ❌ Without Cache

```text
👤 Client
    │
    ▼
🌱 Spring Boot
    │
    ▼
🗄️ Database
    │
    ▼
📦 Data
    │
    ▼
👤 Client
```

Every request hits the database.

### ✅ With Cache

```text
👤 Client
    │
    ▼
🌱 Spring Boot
    │
    ▼
⚡ Redis
    │
    ├── 🟢 HIT  → Return cached data
    │
    └── 🔴 MISS
           │
           ▼
        🗄️ Database
           │
           ▼
        ⚡ Redis
           │
           ▼
        📦 Return data
```

### 🎯 Why caching?

* ⚡ Lower latency
* 🗄️ Reduce database load
* 📈 Improve scalability
* 🔁 Avoid repeated expensive operations

---

# ⚡ 2. What is Redis?

**Redis** is an in-memory data store commonly used for:

* ⚡ Caching
* 🔐 Session storage
* 🔒 Distributed locks
* 🚦 Rate limiting
* 📢 Pub/Sub
* 📦 Temporary data

The basic Redis model is:

```text
🔑 KEY → 💾 VALUE
```

But internally, Redis works with **bytes**.

```text
Java Object
    ↓
🔄 Serializer
    ↓
💾 Bytes
    ↓
⚡ Redis
```

And when retrieving:

```text
⚡ Redis
    ↓
💾 Bytes
    ↓
🔄 Deserializer
    ↓
☕ Java Object
```

---

# 🧱 3. Redis Data Structures

Redis provides several useful data structures:

| Redis Type    | Typical Usage           |
| ------------- | ----------------------- |
| 📝 String     | Simple values / caching |
| 📋 List       | Queues / ordered data   |
| 🧩 Set        | Unique values           |
| 🗂️ Hash      | Object-like data        |
| 🏆 Sorted Set | Rankings / leaderboards |

For basic object caching, the common operation is:

```text
SET → Store
GET → Retrieve
DEL → Delete
```

---

# 🔌 4. Redis + Spring Boot

### Dependency

```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
```

### Configuration

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

Spring Boot can automatically configure the Redis connection.

---

# 🛠️ 5. RedisTemplate

Before using Spring's caching annotations, I implemented caching manually using `RedisTemplate`.

```java
RedisTemplate<String, Object>
```

Conceptually:

```text
             RedisTemplate
                  │
       ┌──────────┼──────────┐
       ▼          ▼          ▼
   Value Ops   List Ops   Hash Ops
       │
       ▼
   SET / GET
```

For caching an entire object/list:

```java
redisTemplate
    .opsForValue()
    .set("jokes:all", jokes);
```

Retrieve:

```java
redisTemplate
    .opsForValue()
    .get("jokes:all");
```

Delete:

```java
redisTemplate.delete("jokes:all");
```

---

# 🔄 6. Serialization

Redis cannot directly store:

```java
Joke joke;
```

The object needs to be serialized.

```text
☕ Joke
   │
   ▼
🔄 Serializer
   │
   ▼
📄 JSON / Bytes
   │
   ▼
⚡ Redis
```

Retrieving:

```text
⚡ Redis
   │
   ▼
📄 JSON / Bytes
   │
   ▼
🔄 Deserializer
   │
   ▼
☕ Joke
```

### ⚠️ Important: `LinkedHashMap` problem

While practicing serialization, a cached object was retrieved as:

```text
LinkedHashMap
```

instead of:

```text
Joke
```

because the deserializer didn't have enough runtime type information.

For example:

```text
JSON Object
    ↓
Jackson
    ↓
LinkedHashMap
```

instead of:

```text
JSON Object
    ↓
Jackson + Joke.class
    ↓
Joke
```

For collections:

```text
List<Joke>
```

is more complicated because:

```java
List<Joke>.class
```

doesn't exist due to **Java type erasure**.

---

# 🔥 7. Cache-Aside Pattern

The first implementation used the **Cache-Aside Pattern**.

Example:

```java
public Joke getJoke(Long id) {

    Joke cached = redisService.get(
        "joke:" + id,
        Joke.class
    );

    if (cached != null) {
        return cached;
    }

    Joke joke = jokeRepository
        .findById(id)
        .orElseThrow();

    redisService.set(
        "joke:" + id,
        joke,
        120L
    );

    return joke;
}
```

### 🔄 Flow

```text
             📥 Request
                 │
                 ▼
          ⚡ Redis GET
                 │
          ┌──────┴──────┐
          │             │
       🟢 HIT        🔴 MISS
          │             │
          ▼             ▼
       Return         🗄️ DB
                        │
                        ▼
                    ⚡ Redis SET
                        │
                        ▼
                      Return
```

---

# 🟢 Cache Hit

When the requested data already exists:

```text
📥 Request
    ↓
⚡ Redis
    ↓
🟢 HIT
    ↓
📦 Return cached data
```

The database isn't queried.

---

# 🔴 Cache Miss

When the data doesn't exist:

```text
📥 Request
    ↓
⚡ Redis
    ↓
🔴 MISS
    ↓
🗄️ Database
    ↓
📦 Data
    ↓
⚡ Redis SET
    ↓
👤 Response
```

---

# ⏳ 8. TTL — Time To Live

Cached data shouldn't necessarily stay forever.

TTL automatically expires cached data after a specified duration.

```java
redisTemplate.opsForValue().set(
    "joke:1",
    joke,
    120,
    TimeUnit.SECONDS
);
```

Meaning:

```text
joke:1
  │
  ├── ⏳ 120 seconds
  │
  └── 💥 Automatically expires
```

### Example

```text
Request #1
🔴 MISS → DB → Redis

Request #2
🟢 HIT → Redis

Request #3
🟢 HIT → Redis

⏳ TTL expires

Request #4
🔴 MISS → DB → Redis
```

---

# 🧹 9. Cache Invalidation

Caching introduces another problem:

> **What happens when the database changes?**

Suppose:

```text
🗄️ Database
Answer = "Old answer"

⚡ Redis
Answer = "Old answer"
```

Database gets updated:

```text
🗄️ Database → "New answer" ✅
⚡ Redis    → "Old answer" ❌
```

Now Redis contains **stale data**.

Therefore the cache needs to be invalidated or updated.

Manual invalidation:

```java
redisService.remove("joke:" + id);
```

---

# ⚠️ 10. Multiple Cache Representations

The project cached both:

```text
jokes:all → List<Joke>

joke:1 → Joke
joke:2 → Joke
joke:3 → Joke
```

This creates an important consistency problem.

Suppose Joke `2` changes:

```text
joke:2
   ↓
🟢 NEW
```

but:

```text
jokes:all
   ↓
🔴 OLD LIST
```

Therefore an update needs to consider **every cached representation** of that data.

```text
✏️ UPDATE JOKE
      │
      ├── ⚡ Update / evict joke:{id}
      │
      └── ⚡ Evict jokes:all
```

---

# 🌱 11. Spring Cache Abstraction

After understanding manual caching, caching was implemented using Spring's abstraction.

Enable caching:

```java
@Configuration
@EnableCaching
public class CacheConfig {
}
```

Spring then provides annotations such as:

```text
@Cacheable
@CachePut
@CacheEvict
@Caching
```

---

# 🎯 12. `@Cacheable`

`@Cacheable` checks the cache **before executing the method**.

```java
@Cacheable(
    value = "joke",
    key = "#id"
)
public Joke getJoke(Long id) {

    return jokeRepository
        .findById(id)
        .orElseThrow();
}
```

### First request

```text
📥 GET /jokes/1
       ↓
⚡ joke::1
       ↓
🔴 MISS
       ↓
☕ Method executes
       ↓
🗄️ Database
       ↓
⚡ Cache result
       ↓
📦 Response
```

### Second request

```text
📥 GET /jokes/1
       ↓
⚡ joke::1
       ↓
🟢 HIT
       ↓
📦 Return cached result
```

The method **does not execute on a cache hit**.

---

# 🧠 13. Cache Name vs Cache Key

This:

```java
@Cacheable(
    value = "joke",
    key = "#id"
)
```

contains two different concepts.

### Cache name

```text
joke
```

### Cache key

```text
#id
```

For:

```text
id = 5
```

the resulting Redis key is conceptually:

```text
joke::5
```

For the complete list:

```java
@Cacheable(
    value = "jokes",
    key = "'all'"
)
public List<Joke> getAllJokes() {
    return jokeRepository.findAll();
}
```

Conceptually:

```text
jokes::all
```

---

# 🔄 14. `@CachePut`

`@CachePut` is different from `@Cacheable`.

```java
@CachePut(
    value = "joke",
    key = "#id"
)
public Joke updateJoke(Long id, String answer) {

    Joke joke = jokeRepository
        .findById(id)
        .orElseThrow();

    joke.setAnswer(answer);

    return joke;
}
```

### Flow

```text
📥 UPDATE
   ↓
@CachePut
   ↓
☕ Method ALWAYS executes
   ↓
🗄️ Database update
   ↓
📦 Return Joke
   ↓
⚡ Put returned Joke into cache
```

### Key difference

```text
@Cacheable
    ↓
🟢 HIT → Method skipped

@CachePut
    ↓
🚀 Method ALWAYS executes
    ↓
📦 Result → Cache
```

---

# 🗑️ 15. `@CacheEvict`

Used when cached data needs to be removed.

```java
@CacheEvict(
    value = "joke",
    key = "#id"
)
public void deleteJoke(Long id) {

    jokeRepository.deleteById(id);
}
```

Flow:

```text
🗑️ DELETE
    ↓
🗄️ Database delete
    ↓
⚡ Remove joke::id
```

For removing an entire cache:

```java
@CacheEvict(
    value = "jokes",
    allEntries = true
)
```

---

# 🧩 16. `@Caching`

Sometimes one operation affects multiple caches.

Example:

```java
@Caching(
    put = @CachePut(
        value = "joke",
        key = "#id"
    ),
    evict = @CacheEvict(
        value = "jokes",
        key = "'all'"
    )
)
@Transactional
public Joke updateJoke(Long id, String answer) {

    Joke joke = jokeRepository
        .findById(id)
        .orElseThrow();

    joke.setAnswer(answer);

    return joke;
}
```

### Why?

Because we have:

```text
joke::5
   ↓
individual Joke

jokes::all
   ↓
List<Joke>
```

After updating Joke `5`:

```text
🗄️ DB
 │
 ├── ✏️ Update Joke 5
 │
 ├── ⚡ joke::5 → NEW
 │
 └── 🗑️ jokes::all → EVICT
```

---

# 🏗️ 17. Spring Caching Internals

The biggest difference between the manual and annotation-based approaches is **who performs the cache logic**.

### Manual

```text
Controller
    ↓
Service
    ↓
RedisService
    ↓
RedisTemplate
    ↓
Redis
```

You manually write:

```java
if (cached != null) {
    return cached;
}
```

### Spring Cache

```text
Controller
    ↓
🌱 Spring Proxy
    ↓
CacheInterceptor
    ↓
CacheManager
    ↓
RedisCache
    ↓
⚡ Redis
```

Conceptually:

```text
@Cacheable
    ↓
Spring intercepts method call
    ↓
Check CacheManager
    ↓
     ┌─────────────┐
     │             │
   🟢 HIT        🔴 MISS
     │             │
     ↓             ↓
 Return        Execute method
 cache             ↓
                Database
                   ↓
               Put cache
                   ↓
                 Return
```

This is why `@Cacheable` isn't simply a "Redis annotation".

It belongs to **Spring's caching abstraction**.

Redis is the actual cache provider underneath.

---

# 🧠 18. Important Mental Model

Remember this distinction:

```text
Spring Cache
     ↓
ABSTRACTION
     ↓
CacheManager
     ↓
RedisCache
     ↓
Redis
     ↓
Actual storage
```

So:

```java
@Cacheable
```

doesn't directly mean:

```text
"Call Redis GET"
```

It means:

> **"Use the configured Spring Cache implementation to retrieve/store this method's result."**

With Redis configured, that implementation is backed by Redis.

---

# ⚔️ Manual vs Spring Cache

| Manual Redis                 | Spring Cache                     |
| ---------------------------- | -------------------------------- |
| `RedisTemplate`              | `@Cacheable`                     |
| Manual HIT/MISS logic        | Spring handles it                |
| Manual serialization         | Cache infrastructure             |
| Manual `SET`                 | Spring handles it                |
| Manual `DELETE`              | `@CacheEvict`                    |
| Manual TTL                   | Cache configuration              |
| More control                 | Less boilerplate                 |
| Good for understanding Redis | Good for application development |

### The key lesson

```text
🧑‍💻 First understand Redis manually
              ↓
🧠 Understand caching mechanics
              ↓
🌱 Use Spring Cache abstraction
              ↓
🚀 Remove boilerplate
```

---

# 🎯 What I Practiced

```text
✅ Redis fundamentals
✅ RedisTemplate
✅ Redis GET / SET / DELETE
✅ Serialization
✅ Deserialization
✅ Cache-aside pattern
✅ Cache hit / miss
✅ TTL
✅ Cache invalidation
✅ Stale cache problem
✅ Object caching
✅ List caching
✅ @EnableCaching
✅ @Cacheable
✅ @CachePut
✅ @CacheEvict
✅ @Caching
✅ Cache names vs keys
✅ Spring Cache abstraction
✅ Basic Spring caching internals
```

---

## 🚀 Final Mental Model

```text
                 🌐 REQUEST
                     │
                     ▼
              🌱 SPRING BOOT
                     │
                     ▼
              🧠 CACHE LAYER
                     │
              ┌──────┴──────┐
              │             │
           🟢 HIT         🔴 MISS
              │             │
              ▼             ▼
         ⚡ REDIS        🗄️ DATABASE
              │             │
              │             ▼
              │        ⚡ STORE CACHE
              │             │
              └──────┬──────┘
                     ▼
                 📦 RESPONSE
```

> **Cache fast data, not stale data.**
> The hard part of caching isn't putting data into Redis — it's deciding **what to cache, when to expire it, and how to keep it consistent with the database.**
