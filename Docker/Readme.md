# 🐳 Docker — First Principles to Production



> **Goal:** Understand Docker so well that commands like `docker run`, Dockerfiles, images, containers, networks, volumes, and Compose all become logical consequences—not things you memorize.

---

# 🧠 1. Why Docker Exists

Imagine you build a Spring Boot application.

On **your machine**:

```text
💻 Windows
 ├── Java 17
 ├── MySQL 8.4
 ├── Maven/Gradle
 ├── environment variables
 └── your application
```

You send it to your friend.

Their machine:

```text
💻 Linux
 ├── Java 21 ❌
 ├── MySQL 5.7 ❌
 ├── different configuration ❌
 └── application 💥
```

Classic:

> **"It works on my machine."** 😭

Docker attacks this problem by packaging the application's **runtime environment** in a reproducible way.

---

# 🧩 2. What Does an Application Actually Need?

Your application isn't just:

```text
MyApp.jar
```

It needs an environment.

For your Spring Boot application:

```text
                    🏠 Runtime Environment
                           │
        ┌──────────────────┼──────────────────┐
        ↓                  ↓                  ↓
     OS/runtime         Java 17           Libraries
        │                  │                  │
        ↓                  ↓                  ↓
   Linux userspace       JVM             Spring Boot
                                           │
                                           ↓
                                       MyApp.jar
```

And your application might additionally need:

```text
🗄️ MySQL
🔴 Redis
📨 Kafka
📁 persistent storage
🌐 network connectivity
```

Docker gives you a standardized way to package and run these environments.

---

# 🐳 3. What Actually Is Docker?

At the simplest level:

> **Docker is a platform for building, distributing, and running applications in isolated environments called containers.**

The three words you need to understand:

```text
📦 IMAGE
   ↓
🏃 CONTAINER
   ↓
🌐 NETWORK / 💾 VOLUME / ⚙️ CONFIG
```

---

# 🧠 4. The Big Docker Mental Model

Think of Docker like this:

```text
                         🐳 DOCKER
                            │
          ┌─────────────────┼──────────────────┐
          ↓                 ↓                  ↓
       📦 Images        🏃 Containers       🛠️ Docker Engine
          │                 │
          │                 ├── Network 🌐
          │                 ├── Volume 💾
          │                 └── Ports 🔌
          │
          ↓
     Dockerfile
```

And Compose sits above this:

```text
                 📝 docker-compose.yml
                         │
              ┌──────────┼──────────┐
              ↓          ↓          ↓
            🐳 App     🐳 MySQL   🐳 Redis
              │          │          │
              └──────────┼──────────┘
                         ↓
                    🌐 Network
```

---

# 🧱 5. Docker Engine

When you install Docker Desktop on Windows, you don't magically turn Windows into Linux.

Docker needs a Linux environment because Linux containers depend on Linux kernel features.

Conceptually:

```text
💻 Windows
   │
   │
   ▼
🐳 Docker Desktop
   │
   ▼
🐧 Linux environment / VM
   │
   ▼
Docker Engine
   │
   ├── containers
   ├── images
   ├── networks
   └── volumes
```

### Important distinction

Your containers aren't usually running their own complete Linux kernel.

Instead:

```text
Container
   ↓
Linux userspace
   ↓
Linux kernel
```

Containers **share the kernel** of the underlying Linux environment.

That's one reason containers are much lighter than full virtual machines.

---

# 🆚 6. VM vs Container

### Virtual Machine

```text
💻 Host OS
   ↓
Hypervisor
   ↓
🖥️ VM
   ├── Guest OS
   ├── Kernel
   ├── Libraries
   └── Application
```

Every VM has its own operating-system environment.

### Container

```text
💻 Host
   ↓
🐳 Docker Engine
   ↓
📦 Container
   ├── userspace
   └── application
         ↓
      shared kernel
```

So:

> **Container ≠ tiny virtual machine.**

That's a huge concept.

---

# 📦 7. What Is a Docker Image?

An image is a **read-only packaged filesystem + metadata** used to create containers.

Think:

```text
📦 IMAGE
=
🏠 filesystem
+
⚙️ configuration
+
📋 metadata
```

For example:

```text
📦 eclipse-temurin:17
        │
        ├── Linux userspace
        ├── Java 17
        ├── required files
        └── metadata
```

Or:

```text
📦 mysql:8.4
        │
        ├── Linux userspace
        ├── MySQL server
        ├── configuration
        └── metadata
```

---

# 🧠 8. Images Are Layered

Images aren't necessarily one giant blob.

Conceptually:

```text
┌─────────────────────────┐
│ Your application        │ ← layer
├─────────────────────────┤
│ Java 17                 │ ← layer
├─────────────────────────┤
│ Runtime files           │ ← layer
├─────────────────────────┤
│ Base image              │ ← layer
└─────────────────────────┘
```

This allows Docker to reuse layers.

That's why building images can be efficient.

---

# 📝 9. Dockerfile

A Dockerfile is simply:

> **A recipe for building an image.**

Example:

```dockerfile
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

Read it as:

```text
FROM
 ↓
Start with Java 17 runtime

WORKDIR
 ↓
Make /app the working directory

COPY
 ↓
Put my JAR inside the image

EXPOSE
 ↓
Document that the app listens on 8080

ENTRYPOINT
 ↓
When container starts:
run java -jar app.jar
```

---

# 🔨 10. Building an Image

Command:

```bash
docker build -t library-app .
```

Break it:

```text
docker
  ↓
build
  ↓
-t library-app     → name/tag
  ↓
.
  ↓
current directory = build context
```

Docker reads:

```text
Dockerfile
```

and produces:

```text
📦 library-app
```

---

# 🔍 11. What Does `.` Mean?

This is extremely important.

```bash
docker build -t library-app .
                              ↑
```

`.` means:

> **Use the current directory as the build context.**

Docker can access files from this context when executing instructions such as:

```dockerfile
COPY
ADD
```

---

# 🏷️ 12. Image Tags

You can have:

```text
library-app:latest
library-app:1.0
library-app:2.0
```

General form:

```text
repository:tag
```

Example:

```bash
docker build -t library-app:1.0 .
```

---

# 🏃 13. What Is a Container?

An image is **not running**.

A container is a **running/instantiated environment created from an image**.

Think:

```text
📦 IMAGE
   │
   │ docker run
   ▼
🏃 CONTAINER
```

Like:

```text
📄 Class
   ↓
🏃 Object
```

Not exactly the same technically, but useful mentally.

---

# 🚀 14. `docker run`

```bash
docker run library-app
```

Conceptually:

```text
📦 library-app image
        │
        │ create
        ▼
🏃 container
        │
        │ start
        ▼
java -jar app.jar
```

---

# 🔌 15. Container Ports

Suppose Spring Boot listens on:

```text
8080
```

inside the container.

That does **not automatically mean Windows can access it**.

You need:

```bash
docker run -p 8080:8080 library-app
```

Meaning:

```text
Windows                Container
  │                       │
  │ localhost:8080        │ :8080
  └──────────────────────>│
```

General:

```text
-p HOST_PORT:CONTAINER_PORT
```

---

# 🧠 16. Why `3307:3306`?

Your Docker MySQL:

```text
MySQL inside container
       ↓
     :3306
```

But Windows already had MySQL using another port.

So:

```text
Windows :3307
      ↓
Docker MySQL :3306
```

Therefore:

### From Windows/Workbench

```text
localhost:3307
```

### From another Docker container

```text
mysql:3306
```

🔥 **This distinction is essential.**

---

# 🌐 17. Docker Networking

Suppose:

```text
🐳 app
🐳 mysql
```

are on the same Docker network.

Docker provides internal DNS.

So instead of:

```text
jdbc:mysql://localhost:3306/library
```

your Spring application uses:

```text
jdbc:mysql://mysql:3306/library
```

because:

```text
mysql
 ↓
Docker DNS
 ↓
MySQL container IP
```

---

# 🚨 18. Why `localhost` Changes Meaning

Inside your Spring container:

```text
localhost
   ↓
THE SPRING CONTAINER
```

It does **not** mean:

```text
Windows
```

So:

```text
jdbc:mysql://localhost:3307
```

inside the app container means:

> Find MySQL inside the app container on port 3307.

Wrong.

Instead:

```text
jdbc:mysql://mysql:3306/library
```

means:

> Find the container named `mysql` on the Docker network.

---

# 🔗 19. Container-to-Container Communication

```text
                🌐 Docker Network
                     │
       ┌─────────────┴─────────────┐
       │                           │
       ▼                           ▼
  🐳 app                      🐳 mysql
  :8080                       :3306
       │                           ▲
       └──── mysql:3306 ───────────┘
```

No host port mapping is required for containers to communicate with each other.

Port mapping is primarily for reaching a container **from outside that Docker network**.

---

# 💾 20. Why Volumes Exist

Containers are disposable.

Imagine:

```text
🐳 MySQL container
       │
       └── database
```

Delete the container:

```text
docker rm mysql
```

Without persistent storage:

```text
💥 database gone
```

Volumes solve this.

```text
🐳 MySQL container
       │
       ▼
    💾 Volume
       │
       ▼
   database files
```

Container can disappear:

```text
🐳 ❌
```

but:

```text
💾 ✅
```

---

# 📦 21. Image vs Container vs Volume

Remember this:

```text
📦 IMAGE
Blueprint
   │
   │ creates
   ▼
🏃 CONTAINER
Running environment
   │
   │ stores persistent data in
   ▼
💾 VOLUME
Persistent data
```

---

# 🧹 22. Container Lifecycle

A container can:

```text
create
  ↓
start
  ↓
running
  ↓
stop
  ↓
stopped
  ↓
start again
```

Commands:

```bash
docker create
docker start
docker stop
docker restart
docker rm
```

Shortcut:

```bash
docker run
```

usually creates **and starts** a container.

---

# 🔎 23. Essential Container Commands

### See running containers

```bash
docker ps
```

### See all containers

```bash
docker ps -a
```

### Stop

```bash
docker stop container-name
```

### Start

```bash
docker start container-name
```

### Remove

```bash
docker rm container-name
```

### Logs

```bash
docker logs container-name
```

Follow logs:

```bash
docker logs -f container-name
```

---

# 🖥️ 24. `docker exec`

Suppose your container is running.

```bash
docker exec -it app bash
```

You're basically saying:

```text
Docker
 ↓
execute a command
 ↓
inside existing container
 ↓
interactive terminal
```

`-i`:

```text
interactive
```

`-t`:

```text
pseudo-terminal
```

Together:

```bash
-it
```

gives you an interactive shell.

Example:

```bash
docker exec -it mysql bash
```

Now:

```text
Windows Terminal
       │
       ▼
🐳 MySQL container
       │
       ▼
🐚 shell
```

---

# 🧪 25. `docker run -it`

Different from `exec`.

```bash
docker run -it ubuntu bash
```

means:

> Create a NEW container and start `bash` interactively.

Whereas:

```bash
docker exec -it existing-container bash
```

means:

> Enter an ALREADY RUNNING container.

---

# 🗑️ 26. What Does `--rm` Do?

```bash
docker run --rm docker-learning
```

Normally:

```text
container finishes
       ↓
container remains stopped
```

With:

```bash
--rm
```

```text
container finishes
       ↓
Docker automatically removes it 🗑️
```

Perfect for temporary containers.

---

# 📦 27. Pulling an Image

```bash
docker pull mysql:8.4
```

Conceptually:

```text
Docker Hub 🌐
      │
      │ pull
      ▼
📦 mysql:8.4
      │
      ▼
Your machine
```

Important:

> **Pulling an image does NOT create a container.**

It only downloads the image.

---

# 🏃 28. Pull → Run

```bash
docker pull mysql:8.4
```

gives:

```text
📦 mysql image
```

Then:

```bash
docker run mysql:8.4
```

creates:

```text
🏃 mysql container
```

---

# 🧹 29. Images vs Containers

You can have:

```text
📦 mysql:8.4
   │
   ├── 🏃 mysql-1
   ├── 🏃 mysql-2
   └── 🏃 mysql-3
```

One image can create many containers.

---

# 🔍 30. Image Commands

```bash
docker images
```

or:

```bash
docker image ls
```

Remove:

```bash
docker rmi image-name
```

Pull:

```bash
docker pull image:tag
```

Inspect:

```bash
docker image inspect image-name
```

---

# 🧬 31. Dockerfile Instructions

## `FROM`

```dockerfile
FROM eclipse-temurin:17-jre
```

Base image.

---

## `WORKDIR`

```dockerfile
WORKDIR /app
```

Sets working directory.

---

## `COPY`

```dockerfile
COPY app.jar app.jar
```

Copies files from build context into image.

---

## `RUN`

```dockerfile
RUN apt-get update
```

Runs a command **while building the image**.

Important:

```text
RUN
 ↓
BUILD TIME
```

---

## `CMD`

```dockerfile
CMD ["java", "-jar", "app.jar"]
```

Default command.

---

## `ENTRYPOINT`

```dockerfile
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Defines the executable for the container.

Mental model:

```text
RUN        → build time 🔨
CMD        → default runtime command ▶️
ENTRYPOINT → main runtime executable 🚀
```

---

## `EXPOSE`

```dockerfile
EXPOSE 8080
```

Documents the intended container port.

It **does not publish the port by itself**.

You still need:

```bash
-p 8080:8080
```

---

# 🏗️ 32. Your Spring Boot Dockerfile

A simple version:

```dockerfile
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY build/libs/library-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build:

```bash
docker build -t library-app .
```

Run:

```bash
docker run -p 8080:8080 library-app
```

---

# 🐳 33. Why Docker Compose?

Imagine your application needs:

```text
Spring Boot
MySQL
Redis
Kafka
```

Running everything manually:

```bash
docker run ...
docker run ...
docker run ...
docker run ...
```

😵‍💫

Compose lets you describe the whole system declaratively.

```yaml
services:

  app:
    ...

  mysql:
    ...

  redis:
    ...
```

Then:

```bash
docker compose up
```

🔥 One command.

---

# 🧩 34. Docker Compose Mental Model

```text
             docker-compose.yml
                     │
        ┌────────────┼────────────┐
        ↓            ↓            ↓
    🐳 app        🐳 mysql      🐳 redis
        │            │            │
        └────────────┼────────────┘
                     ↓
                🌐 network
```

Compose manages the **multi-container application**.

---

# 📝 35. Your Library Compose

```yaml
services:

  mysql:
    image: mysql:8.4

    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: library

    ports:
      - "3307:3306"

    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-uroot", "-proot"]
      interval: 5s
      timeout: 5s
      retries: 10

  app:
    build: .

    ports:
      - "8080:8080"

    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/library
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root

    depends_on:
      mysql:
        condition: service_healthy
```

---

# 🔥 36. What Happens When You Run `docker compose up`?

This is the part you should **visualize**.

```bash
docker compose up
```

Docker reads:

```text
docker-compose.yml
        │
        ▼
"Need mysql"
        │
        ▼
📦 mysql:8.4
        │
        ▼
🏃 mysql container
```

Then:

```text
"Need app"
    │
    ▼
Dockerfile
    │
    ▼
🔨 docker build
    │
    ▼
📦 app image
    │
    ▼
🏃 app container
```

Compose creates:

```text
🌐 library_default network
```

Then:

```text
app ──────── mysql:3306 ────────> mysql
```

---

# ❤️ 37. `depends_on`

```yaml
depends_on:
  mysql:
    condition: service_healthy
```

Means:

```text
MySQL container
      ↓
healthcheck
      ↓
healthy ✅
      ↓
start app
```

Without health checking:

```text
container exists
      ≠
application inside is ready
```

This is a **real production concept**.

---

# ❤️‍🩹 38. Healthcheck

```yaml
healthcheck:
  test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
  interval: 5s
  timeout: 5s
  retries: 10
```

Docker periodically asks:

> "Are you healthy?"

Possible state:

```text
starting 🟡
healthy 🟢
unhealthy 🔴
```

---

# ⚙️ 39. Environment Variables

Don't hardcode everything into your image.

Instead:

```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/library
```

Spring Boot can consume it as:

```text
SPRING_DATASOURCE_URL
        ↓
spring.datasource.url
```

This gives you:

```text
Same image 📦
       │
       ├── development ⚙️
       ├── staging ⚙️
       └── production ⚙️
```

without rebuilding the application image just because the database URL changes.

---

# 💾 40. Compose Volumes

Example:

```yaml
volumes:
  mysql-data:
```

Then:

```yaml
mysql:
  volumes:
    - mysql-data:/var/lib/mysql
```

Mental model:

```text
🐳 MySQL
   │
   ▼
💾 mysql-data
   │
   ▼
persistent database
```

---

# 🗑️ 41. `docker compose down`

```bash
docker compose down
```

Stops/removes the Compose containers and network.

Volumes normally remain unless you specify:

```bash
docker compose down -v
```

⚠️ `-v` can delete your persistent database volume.

---

# 🔄 42. Development vs Production

A common misconception:

> "Docker means I must develop inside Docker."

No.

You can develop normally:

```text
IDE
 ↓
Spring Boot locally
 ↓
MySQL Docker
```

Or:

```text
IDE
 ↓
Docker Compose
 ↓
Spring + MySQL + Redis
```

Both are valid.

Teams often use Compose during development when the application has several infrastructure dependencies.

---

# ☁️ 43. Why Docker If Production Uses Cloud?

Excellent question.

Cloud infrastructure might look like:

```text
☁️ AWS / Azure / GCP
       │
       ├── Compute
       ├── Managed MySQL
       ├── Redis
       └── Load Balancer
```

Docker gives you a **standard application artifact**:

```text
📦 Your application image
```

That image can run on:

```text
💻 Developer machine
CI/CD
☁️ Cloud
Kubernetes
VM
container platform
```

So Docker isn't replacing cloud infrastructure.

It's standardizing **how your application is packaged and run**.

---

# 🏭 44. Production Flow

Typical pipeline:

```text
👨‍💻 Developer
      │
      ▼
Git push
      │
      ▼
⚙️ CI/CD
      │
      ▼
🔨 Docker build
      │
      ▼
📦 Docker image
      │
      ▼
📦 Container Registry
      │
      ▼
☁️ Cloud
      │
      ▼
🏃 Container
```

---

# 🏗️ 45. Docker Image vs Docker Container

| 📦 Image               | 🏃 Container             |
| ---------------------- | ------------------------ |
| Blueprint              | Running instance         |
| Read-only layers       | Writable container layer |
| Can be distributed     | Runs application         |
| Built using Dockerfile | Created from image       |
| Doesn't execute itself | Executes process         |

Think:

```text
Dockerfile
    ↓ build
📦 Image
    ↓ run
🏃 Container
```

---

# 🧠 46. The Complete Mental Model

This is the diagram I'd memorize:

```text
                         🐳 DOCKER
                            │
                            ▼
                     Docker Engine
                            │
          ┌─────────────────┼─────────────────┐
          │                 │                 │
          ▼                 ▼                 ▼
       📦 Images       🌐 Networks       💾 Volumes
          │
          │ docker run
          ▼
     🏃 Containers
          │
     ┌────┼──────────────┐
     │    │              │
     ▼    ▼              ▼
    App  MySQL          Redis
     │    │              │
     └────┴──────🌐──────┘
```

And image creation:

```text
Dockerfile
    │
    │ docker build
    ▼
📦 Image
    │
    │ docker run
    ▼
🏃 Container
```

And multi-container application:

```text
docker-compose.yml
        │
        │ docker compose up
        ▼
┌──────────────────────────┐
│       Docker             │
│                          │
│ 🏃 App                   │
│    │                     │
│    │ mysql:3306          │
│    ▼                     │
│ 🏃 MySQL                 │
│    │                     │
│    ▼                     │
│ 💾 Volume                │
└──────────────────────────┘
```

---

# 🧰 47. Commands Cheat Sheet

### Images

```bash
docker pull IMAGE
docker images
docker image ls
docker build -t NAME .
docker rmi IMAGE
docker image inspect IMAGE
```

### Containers

```bash
docker run IMAGE
docker run -d IMAGE
docker run -p 8080:8080 IMAGE
docker run --rm IMAGE
docker ps
docker ps -a
docker stop CONTAINER
docker start CONTAINER
docker restart CONTAINER
docker rm CONTAINER
docker logs CONTAINER
docker logs -f CONTAINER
docker exec -it CONTAINER bash
```

### Networks

```bash
docker network ls
docker network inspect NETWORK
docker network create NETWORK
docker network rm NETWORK
```

### Volumes

```bash
docker volume ls
docker volume inspect VOLUME
docker volume create VOLUME
docker volume rm VOLUME
```

### Compose

```bash
docker compose up
docker compose up -d
docker compose up --build
docker compose down
docker compose down -v
docker compose ps
docker compose logs
docker compose logs -f
docker compose exec SERVICE bash
```

---

# 🧠 48. The 10 Things You MUST Understand

If you understand these, you're good:

```text
1️⃣ Docker Engine
2️⃣ Image
3️⃣ Container
4️⃣ Dockerfile
5️⃣ Container lifecycle
6️⃣ Port mapping
7️⃣ Docker networking + DNS
8️⃣ Volumes
9️⃣ Environment variables
🔟 Docker Compose
```

Don't try to memorize 100 Docker commands.

Understand the model:

```text
📜 Dockerfile
      ↓
   🔨 build
      ↓
   📦 Image
      ↓
   🚀 run
      ↓
 🏃 Container
      │
      ├── 🌐 Network
      ├── 🔌 Ports
      ├── 💾 Volumes
      └── ⚙️ Environment
```

Then:

```text
                    docker-compose.yml
                            ↓
                      docker compose
                            ↓
              ┌─────────────┼─────────────┐
              ↓             ↓             ↓
           🏃 App        🏃 MySQL      🏃 Redis
              │             │             │
              └─────────────┼─────────────┘
                            ↓
                       🌐 Network
```

**That's Docker.** 🐳🔥
