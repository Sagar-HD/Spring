# 🔐 Spring Security --- First Principles & Internals

> **Goal:** Understand Spring Security from first principles --- not
> just memorize annotations/configuration.\
> This folder is my deep-dive into **identity, authentication,
> authorization, security context, filter chains, authentication
> providers, sessions/tokens, and eventually JWT/OAuth2**.

------------------------------------------------------------------------

# 🧭 Learning Map

``` text
Security Fundamentals
        │
        ├── Assets / Threats / Vulnerabilities
        ├── Trust Boundaries
        ├── Identity / Credentials
        ├── Authentication
        ├── Authorization
        └── Stateful vs Stateless
                 │
                 ▼
        Spring Security Core Model
                 │
        ┌────────┴─────────┐
        │                  │
 Authentication       SecurityContext
        │                  │
 Authentication        SecurityContextHolder
 Principal             ThreadLocal
 Authorities
        │
        ▼
      Servlet Filter Architecture
        │
        ▼
DelegatingFilterProxy
        │
        ▼
FilterChainProxy
        │
        ▼
SecurityFilterChain
        │
        ▼
Authentication Filter
        │
        ▼
AuthenticationManager
        │
        ▼
ProviderManager
        │
        ▼
AuthenticationProvider
        │
        └── DaoAuthenticationProvider
                │
                ├── UserDetailsService
                └── PasswordEncoder

NEXT:
JWT → Bearer Tokens → OAuth2 → Resource Server → Authorization Server
```

------------------------------------------------------------------------

# 1. 🧱 Security Fundamentals

Before learning Spring Security, understand the problem it is solving.

## 1.1 Assets

An **asset** is something valuable that must be protected.

Examples:

-   👤 User identity
-   🔑 Passwords / credentials
-   💰 Money
-   📦 Business data
-   🗄️ Database records
-   🛡️ Admin operations
-   📄 Private documents
-   🔐 Session/token information

Security asks:

> **What are we protecting, from whom, and under what conditions?**

------------------------------------------------------------------------

# 2. ⚠️ Threat vs Vulnerability

### Threat

A **threat** is a potential source of harm.

Example:

``` text
Attacker
   ↓
tries to access admin endpoint
```

### Vulnerability

A **vulnerability** is a weakness that allows the threat to succeed.

Example:

``` text
/admin/deleteUser

No authentication
No authorization
        ↓
Anyone can delete users
```

### Simple mental model

``` text
Threat + Vulnerability
        ↓
Potential Attack
        ↓
Impact on Asset
```

------------------------------------------------------------------------

# 3. 🚧 Trust Boundaries

A **trust boundary** is a point where data crosses from a less-trusted
environment into a trusted environment.

Example:

``` text
Browser
  │
  │ HTTP request
  ▼
Spring Boot Application
  │
  │ trusted internal calls
  ▼
Database
```

The application cannot blindly trust:

-   Request parameters
-   Headers
-   Cookies
-   Tokens
-   User input
-   Client-provided roles

The server must establish trust through security mechanisms.

------------------------------------------------------------------------

# 4. 👤 Identity, Credentials & Identity Claims

### Identity

An identity answers:

> **Who is this user?**

Example:

``` text
sagar@example.com
```

### Credential

A credential is evidence used to prove identity.

Examples:

``` text
Password
OTP
Authenticator code
Smart card
Private key
Biometric
Access token
```

### Identity claim

A request can make a claim:

``` text
"I am user Sagar"
```

The server should not trust the claim merely because it exists.

It must authenticate it.

------------------------------------------------------------------------

# 5. 🔑 Authentication

Authentication answers:

> **Who are you?**

Common authentication factors:

### 1️⃣ Something you know

Examples:

-   Password
-   PIN
-   Security question

### 2️⃣ Something you have

Examples:

-   Phone
-   Authenticator app
-   Smart card
-   Hardware security key

### 3️⃣ Something you are

Examples:

-   Fingerprint
-   Face
-   Retina

### MFA

Multi-factor authentication combines independent factors.

``` text
Password
   +
Authenticator OTP
   +
Optional biometric
```

The important idea:

> Two passwords are not necessarily two factors.\
> MFA means combining different categories of evidence.

------------------------------------------------------------------------

# 6. 🛂 Authorization

Authorization answers:

> **What is this authenticated identity allowed to do?**

Example:

``` text
User = Sagar

Can:
    READ_PROFILE
    UPDATE_PROFILE

Cannot:
    DELETE_USERS
    MANAGE_SYSTEM
```

Authentication comes before authorization:

``` text
WHO are you?
     ↓
Authentication
     ↓
WHAT can you do?
     ↓
Authorization
```

------------------------------------------------------------------------

# 7. 🎭 Principal, Authority & Role

These three concepts are extremely important in Spring Security.

## Principal

The **principal** represents the currently authenticated identity.

Example:

``` text
User(id=10, username="sagar")
```

## Authority

An authority represents a permission/capability.

Examples:

``` text
READ_PROFILE
UPDATE_PROFILE
DELETE_USER
CREATE_COURSE
```

In Spring Security:

``` java
GrantedAuthority
```

represents an authority.

## Role

A role is commonly used as a grouping of authorities.

Example:

``` text
ROLE_ADMIN
    ├── READ_USER
    ├── UPDATE_USER
    ├── DELETE_USER
    └── CREATE_USER
```

Think:

``` text
User
 ↓
Role
 ↓
Authorities
 ↓
Allowed actions
```

Important:

> In Spring Security, roles are generally represented as authorities
> with a `ROLE_` prefix.

------------------------------------------------------------------------

# 8. 🚦 Rate Limiting

Authentication is not the only security concern.

Public endpoints such as:

``` text
POST /login
POST /register
POST /forgot-password
```

can be abused.

Example:

``` text
Attacker
   ↓
100,000 login attempts
   ↓
/login
```

Rate limiting can restrict requests:

``` text
5 attempts / minute / IP
```

or use more sophisticated combinations:

``` text
IP + username + device + time window
```

Rate limiting is an **abuse-control mechanism**, not authentication
itself.

------------------------------------------------------------------------

# 9. 🔄 Stateful vs Stateless Authentication

This distinction becomes critical for JWT.

## Stateful

The server maintains authentication state.

``` text
Client
  │
  │ login
  ▼
Server
  │
  └── creates session
         │
         ▼
      Session Store
```

Client sends:

``` text
SESSION_ID
```

Server looks up the state.

### Advantages

-   Easy server-side revocation
-   Small client credential
-   Server controls session lifecycle
-   Natural fit for traditional web applications

### Disadvantages

-   Server-side state
-   Session storage/scaling concerns
-   Distributed deployments may need shared session storage

------------------------------------------------------------------------

# 10. 🪪 Stateless Authentication

The server does not need to store authentication state for every client.

Example:

``` text
Client
  │
  │ Bearer Token
  ▼
Server
  │
  └── validates token
          ↓
     Authentication
```

The information required to establish authentication is carried by the
request/token or obtained from an external token service.

### Advantages

-   Easier horizontal scaling
-   No per-user server session required
-   Natural fit for APIs and distributed systems

### Disadvantages

-   Revocation is harder
-   Token lifecycle becomes important
-   Token leakage can be dangerous
-   Token validation/rotation/key management become important

------------------------------------------------------------------------

# 11. 🎫 Self-Contained vs Opaque Tokens

## Self-contained token

The token contains information needed to establish identity/authorities.

JWT is the common example.

``` text
JWT
 ├── Header
 ├── Claims
 └── Signature
```

Server can validate it without looking up the token itself.

## Opaque token

The token is just an identifier.

``` text
Client
  │
  │ opaque token
  ▼
Application
  │
  │ introspection
  ▼
Authorization Server / Token Service
  │
  ▼
Identity + Authorities
```

The token itself does not necessarily contain readable authentication
information.

------------------------------------------------------------------------

# 12. 🪪 Bearer Token

A bearer token means:

> Whoever possesses the token can potentially use it.

Typical request:

``` http
Authorization: Bearer <token>
```

Therefore:

``` text
Token theft
    ↓
Attacker can impersonate user
```

Tokens must be protected carefully.

------------------------------------------------------------------------

# 13. 🧠 Spring Security Core Model

The central Spring Security object is:

``` java
Authentication
```

It represents the result/state of authentication.

It can also represent an authentication request before authentication
has completed.

------------------------------------------------------------------------

# 14. 🔌 Authentication Interface

Conceptually:

``` java
public interface Authentication extends Principal {

    Collection<? extends GrantedAuthority> getAuthorities();

    Object getCredentials();

    Object getDetails();

    Object getPrincipal();

    boolean isAuthenticated();

    void setAuthenticated(boolean authenticated);
}
```

The exact API should always be checked against the Spring Security
version being used.

### Meaning

``` text
Authentication
│
├── principal
│     └── WHO
│
├── credentials
│     └── proof used during authentication
│
├── authorities
│     └── WHAT they can do
│
├── details
│     └── additional request/authentication information
│
└── authenticated
      └── authentication state
```

------------------------------------------------------------------------

# 15. 🔄 Authentication Request vs Authenticated Authentication

This distinction is crucial.

Before authentication:

``` text
UsernamePasswordAuthenticationToken
        │
        ├── username
        ├── password
        └── authenticated = false
```

This is an **authentication request**.

After successful authentication:

``` text
UsernamePasswordAuthenticationToken
        │
        ├── principal = UserDetails
        ├── authorities = ...
        ├── credentials = ...
        └── authenticated = true
```

So the same general `Authentication` abstraction can represent:

``` text
"I want to authenticate"
        ↓
"I have been authenticated"
```

------------------------------------------------------------------------

# 16. 🧵 SecurityContext

Spring Security needs somewhere to keep:

> **Who is currently authenticated for this execution/request?**

That object is:

``` java
SecurityContext
```

Conceptually:

``` text
SecurityContext
      │
      └── Authentication
              │
              ├── Principal
              ├── Authorities
              └── Authentication state
```

------------------------------------------------------------------------

# 17. 🧵 SecurityContextHolder

Application code commonly accesses the current context through:

``` java
SecurityContextHolder
```

Conceptually:

``` text
SecurityContextHolder
        ↓
SecurityContext
        ↓
Authentication
        ↓
Principal
```

Example:

``` java
Authentication authentication =
    SecurityContextHolder
        .getContext()
        .getAuthentication();
```

------------------------------------------------------------------------

# 18. 🧵 ThreadLocal Mental Model

Traditionally, Spring Security uses a thread-local based strategy.

Conceptually:

``` text
Thread A
 └── SecurityContext A
       └── Authentication A

Thread B
 └── SecurityContext B
       └── Authentication B
```

Therefore request processing on one thread does not accidentally read
another thread's authentication context.

Modern Spring Security also exposes a `SecurityContextHolderStrategy`,
so the storage strategy is configurable rather than conceptually
hard-coded to one mechanism.

------------------------------------------------------------------------

# 19. 🌐 Servlet Request → Spring Security

A request roughly travels like:

``` text
Browser
   │
   ▼
Tomcat
   │
   ▼
Servlet Container Filter Chain
   │
   ▼
DelegatingFilterProxy
   │
   ▼
FilterChainProxy
   │
   ▼
SecurityFilterChain
   │
   ▼
Spring Security Filters
   │
   ▼
DispatcherServlet
   │
   ▼
Controller
```

This is one of the most important architectures to understand.

------------------------------------------------------------------------

# 20. 🪄 DelegatingFilterProxy

The Servlet container knows about Servlet filters.

Spring Security wants its filters managed by Spring.

`DelegatingFilterProxy` acts as the bridge.

``` text
Tomcat
  │
  │ invokes Servlet Filter
  ▼
DelegatingFilterProxy
  │
  │ delegates
  ▼
Spring-managed Filter
```

------------------------------------------------------------------------

# 21. 🧩 FilterChainProxy

`FilterChainProxy` is a central Spring Security filter.

It contains multiple security filter chains.

Conceptually:

``` text
FilterChainProxy
      │
      ├── SecurityFilterChain #1
      │       matcher: /api/**
      │
      ├── SecurityFilterChain #2
      │       matcher: /admin/**
      │
      └── SecurityFilterChain #3
              matcher: /**
```

For a request:

``` text
GET /api/users
```

Spring determines which `SecurityFilterChain` matches.

Only the selected chain is applied.

------------------------------------------------------------------------

# 22. 🔗 SecurityFilterChain

A `SecurityFilterChain` conceptually contains:

``` text
Request Matcher
+
Ordered list of Security Filters
```

The interface exposes a matcher and filters.

Example mental model:

``` text
SecurityFilterChain
│
├── CORS / exploit protection related filters
├── CSRF
├── authentication filters
├── exception translation
├── authorization
└── other security infrastructure
```

**Exact filter ordering depends on the Spring Security version and
configuration.**

------------------------------------------------------------------------

# 23. 🏗️ HttpSecurity & Builder Pattern

We normally configure Spring Security using:

``` java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http
        .authorizeHttpRequests(...)
        .httpBasic(...)
        .formLogin(...);

    return http.build();
}
```

`HttpSecurity` is a builder.

Conceptually:

``` text
HttpSecurity
   ↓
configure security
   ↓
http.build()
   ↓
SecurityFilterChain
```

We don't manually instantiate every filter.

Spring Security creates and wires the required infrastructure.

------------------------------------------------------------------------

# 24. 🧱 Default Security Filter Chain

If we do not define our own security configuration, Spring Boot/Spring
Security can create default security configuration.

The important lesson:

> If you want to control authentication and authorization behavior, you
> normally define your own `SecurityFilterChain` bean.

------------------------------------------------------------------------

# 25. 🔐 Authentication Filters

Authentication filters sit at the boundary where request credentials
become an `Authentication` object.

Two classic examples:

## Form Login

``` text
UsernamePasswordAuthenticationFilter
```

Conceptually:

``` text
username + password
       ↓
Authentication request
       ↓
AuthenticationManager
```

## HTTP Basic

``` text
BasicAuthenticationFilter
```

Conceptually:

``` text
Authorization: Basic ...
       ↓
decode credentials
       ↓
Authentication request
       ↓
AuthenticationManager
```

These filters do not themselves contain all authentication logic.

Their job is largely:

``` text
Extract credentials
        ↓
Create Authentication
        ↓
Delegate authentication
        ↓
Handle success/failure
```

------------------------------------------------------------------------

# 26. 🎯 AuthenticationManager

Core interface:

``` java
public interface AuthenticationManager {

    Authentication authenticate(Authentication authentication);
}
```

Its responsibility:

> Take an authentication attempt and try to authenticate it.

Input:

``` text
Authentication
(authenticated = false)
```

Output:

``` text
Authentication
(authenticated = true)
```

or failure:

``` text
AuthenticationException
```

------------------------------------------------------------------------

# 27. 🏛️ AuthenticationProvider

There can be many authentication mechanisms.

Examples:

``` text
Username + Password
JWT
OTP
LDAP
SAML
API Key
OAuth2
```

Instead of putting everything inside one giant manager, Spring Security
uses:

``` java
AuthenticationProvider
```

Conceptually:

``` java
interface AuthenticationProvider {

    Authentication authenticate(Authentication authentication);

    boolean supports(Class<?> authentication);
}
```

------------------------------------------------------------------------

# 28. 🏗️ ProviderManager

The most important `AuthenticationManager` implementation is:

``` text
ProviderManager
```

It delegates to one or more `AuthenticationProvider`s.

Architecture:

``` text
AuthenticationManager
       ▲
       │ implements
ProviderManager
       │
       ├── AuthenticationProvider
       ├── AuthenticationProvider
       └── AuthenticationProvider
```

Example:

``` text
ProviderManager
     │
     ├── DaoAuthenticationProvider
     ├── JwtAuthenticationProvider
     └── CustomAuthenticationProvider
```

------------------------------------------------------------------------

# 29. 🧠 Provider Selection

Suppose the manager receives:

``` java
UsernamePasswordAuthenticationToken
```

`ProviderManager` checks providers using:

``` java
supports(authentication.getClass())
```

Conceptually:

``` text
Authentication
      │
      ▼
ProviderManager
      │
      ├── Provider A
      │     supports? ❌
      │
      ├── Provider B
      │     supports? ✅
      │
      └── Provider C
            not needed
```

The matching provider performs authentication.

This is how one authentication manager can support many authentication
mechanisms.

------------------------------------------------------------------------

# 30. 🗃️ DaoAuthenticationProvider

For username/password authentication, the classic provider is:

``` text
DaoAuthenticationProvider
```

Its conceptual responsibility:

``` text
Authentication request
        │
        ▼
UserDetailsService
        │
        ▼
UserDetails
        │
        ▼
PasswordEncoder
        │
        ▼
credential validation
        │
        ▼
account checks
        │
        ▼
authenticated Authentication
```

It is an **AuthenticationProvider**, not an `AuthenticationManager`.

------------------------------------------------------------------------

# 31. 👤 UserDetails

Your application's domain entity may look like:

``` java
@Entity
class User {

    Long id;
    String username;
    String password;
    String role;
}
```

Spring Security does not require your entity itself to become the
security abstraction.

Instead we can adapt it.

``` text
Application User
      │
      ▼
CustomUserDetails
      │
      ▼
UserDetails
      │
      ▼
Spring Security
```

This is the Adapter pattern in practice.

------------------------------------------------------------------------

# 32. 🧩 CustomUserDetails

Example concept:

``` java
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // convert application roles/permissions
        // into Spring Security authorities
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

The account-state methods allow Spring Security to model things such as:

``` text
Account expired
Account locked
Credentials expired
Account disabled
```

------------------------------------------------------------------------

# 33. 🔎 UserDetailsService

Spring Security needs a way to load the user.

``` java
public interface UserDetailsService {

    UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException;
}
```

Your implementation:

``` text
Username
   ↓
UserDetailsService
   ↓
Repository
   ↓
Database
   ↓
User entity
   ↓
CustomUserDetails
   ↓
UserDetails
```

The key point:

> `UserDetailsService` does not authenticate the password by itself.

It loads the security representation of the user.

------------------------------------------------------------------------

# 34. 🔐 PasswordEncoder

Password verification is handled through a `PasswordEncoder`.

Conceptually:

``` text
Request password
      │
      ▼
PasswordEncoder.matches(...)
      ▲
      │
Stored encoded password
```

Never store plaintext passwords.

Typical modern choice:

``` text
BCrypt
Argon2
```

depending on application requirements.

------------------------------------------------------------------------

# 35. 🔄 Complete Username/Password Flow

This is the core flow to memorize conceptually.

``` text
                HTTP REQUEST
                     │
                     ▼
          Authentication Filter
                     │
                     │ extracts username/password
                     ▼
        UsernamePasswordAuthenticationToken
                     │
                     │ authenticated = false
                     ▼
           AuthenticationManager
                     │
                     ▼
              ProviderManager
                     │
                     │ supports(...)
                     ▼
        DaoAuthenticationProvider
                     │
                     ▼
            UserDetailsService
                     │
                     ▼
               Repository
                     │
                     ▼
                Database
                     │
                     ▼
             User / UserDetails
                     │
                     ▼
              PasswordEncoder
                     │
             matches(password)
                     │
              ┌──────┴──────┐
              │             │
            FAIL          SUCCESS
              │             │
              ▼             ▼
       Authentication    Authenticated
          Exception      Authentication
              │             │
              ▼             ▼
             401      SecurityContext
                            │
                            ▼
                       Current user
```

------------------------------------------------------------------------

# 36. 🧠 What Exactly Happens After Successful Authentication?

After a provider successfully authenticates:

``` text
Authentication
     │
     ▼
SecurityContext
     │
     ▼
SecurityContextHolder
```

Then later code can access:

``` java
SecurityContextHolder
    .getContext()
    .getAuthentication();
```

The controller/service does not need to re-authenticate the user for
every method call inside the same request.

------------------------------------------------------------------------

# 37. 🔥 Exception Handling Boundary

Authentication and authorization failures must eventually become HTTP
responses.

Common outcomes:

``` text
Unauthenticated
      ↓
401 Unauthorized

Authenticated but forbidden
      ↓
403 Forbidden
```

Spring Security has separate concepts for these.

### AuthenticationEntryPoint

Handles:

> "You are not authenticated."

Usually maps to:

``` text
401
```

### AccessDeniedHandler

Handles:

> "You are authenticated, but you don't have permission."

Usually maps to:

``` text
403
```

The exception translation infrastructure connects security exceptions to
these HTTP responses.

------------------------------------------------------------------------

# 38. 🛡️ Authorization

After authentication:

``` text
Authentication
      │
      ▼
Authorization decision
      │
      ├── allowed → continue
      │
      └── denied → 403
```

Modern Spring Security uses the `AuthorizationManager` model for
authorization decisions.

Authorization can happen at different levels:

``` text
Request level
Method level
Object/resource level
```

Examples:

``` java
.requestMatchers("/admin/**").hasRole("ADMIN")
```

and method-level security such as:

``` java
@PreAuthorize("hasAuthority('READ_USER')")
```

------------------------------------------------------------------------

# 39. 🌐 Request Authorization vs Method Authorization

## Request authorization

Security decision based on HTTP request:

``` text
GET /admin/users
       ↓
Is user ADMIN?
```

## Method authorization

Security decision happens around a Java method:

``` java
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(Long id) {
}
```

This is one place where Spring Security's broader AOP/proxy
infrastructure becomes relevant.

------------------------------------------------------------------------

# 40. 🧱 Exploit Protection

Spring Security is not only authentication.

It also provides defenses around common web attack surfaces.

Important areas:

``` text
CSRF
CORS
Security headers
Session management
Request protection
```

------------------------------------------------------------------------

# 41. 🛡️ CSRF

CSRF = Cross-Site Request Forgery.

The attacker tries to make a user's browser send an unwanted
state-changing request.

Conceptually:

``` text
Victim logged in
      │
      ▼
Malicious website
      │
      ▼
Browser sends request
      │
      ▼
Your application
```

Spring Security's CSRF protection uses a token mechanism.

Safe HTTP methods are generally not protected by CSRF by default:

``` text
GET
HEAD
OPTIONS
TRACE
```

State-changing methods such as:

``` text
POST
PUT
PATCH
DELETE
```

are the usual focus.

CSRF is especially relevant when authentication credentials are
automatically attached by the browser, such as cookies.

For bearer-token APIs where the token is explicitly attached by
JavaScript/client code, CSRF risk is different and configuration may
differ.

------------------------------------------------------------------------

# 42. 🌍 CORS

CORS = Cross-Origin Resource Sharing.

It is primarily a **browser security policy**, not an authentication
mechanism.

Example:

``` text
Frontend
http://localhost:5173

        ↓ cross-origin request

Backend
http://localhost:8080
```

The browser decides whether the frontend is allowed to access the
response.

Important distinction:

``` text
CORS
→ browser cross-origin access control

Authentication
→ who are you?

Authorization
→ what can you do?

CSRF
→ can another origin trick your browser into performing an action?
```

Do not mentally merge these concepts.

------------------------------------------------------------------------

# 43. 🔒 Security Headers

Spring Security can also contribute security-related HTTP headers.

Examples include protections related to:

``` text
Clickjacking
Content type sniffing
Referrer policy
HSTS
Content Security Policy
```

Exact defaults depend on Spring Security version and configuration.

------------------------------------------------------------------------

# 44. 🧑‍💻 Spring Security Without Spring Boot

Spring Boot provides convenient auto-configuration.

Without Boot, you have to understand and configure more of the
infrastructure yourself.

Boot can automatically create/configure things such as:

``` text
Security infrastructure
SecurityFilterChain
AuthenticationManager infrastructure
UserDetailsService
PasswordEncoder
etc.
```

depending on the dependencies and configuration present.

The important learning distinction:

``` text
Spring Security
    =
actual security framework

Spring Boot
    =
configuration / auto-configuration convenience
```

------------------------------------------------------------------------

# 45. ⚙️ Auto-Configuration Mental Model

Instead of thinking:

> "Spring magically does authentication."

Think:

``` text
Spring Boot
    ↓
detects dependencies/configuration
    ↓
creates beans
    ↓
wires Spring Security infrastructure
    ↓
SecurityFilterChain
    ↓
filters
    ↓
AuthenticationManager
    ↓
AuthenticationProviders
```

When learning manually, recreate these relationships yourself.

That exposes what Boot normally hides.

------------------------------------------------------------------------

# 46. 🧬 Complete Hierarchy

The hierarchy to remember:

``` text
Authentication
      ▲
      │
AuthenticationManager
      ▲
      │
ProviderManager
      │
      ├───────────────┐
      ▼               ▼
AuthenticationProvider
      │
      ├── DaoAuthenticationProvider
      ├── JwtAuthenticationProvider
      ├── other providers
      └── custom providers
```

And separately:

``` text
UserDetailsService
      │
      ▼
UserDetails
      │
      ▼
CustomUserDetails
      │
      ▼
Application User Entity
```

These are related, but they are **not the same hierarchy**.

------------------------------------------------------------------------

# 47. 🧠 The Most Important Separation

Do not collapse all of these into one thing.

``` text
Authentication Filter
    ↓
extracts credentials

Authentication
    ↓
represents authentication attempt/result

AuthenticationManager
    ↓
coordinates authentication

ProviderManager
    ↓
delegates to providers

AuthenticationProvider
    ↓
performs a specific authentication mechanism

DaoAuthenticationProvider
    ↓
username/password authentication

UserDetailsService
    ↓
loads user security data

UserDetails
    ↓
Spring Security representation of user

PasswordEncoder
    ↓
checks password

SecurityContext
    ↓
stores current Authentication

AuthorizationManager
    ↓
decides whether authenticated identity can access resource
```

This separation is the backbone of Spring Security.

------------------------------------------------------------------------

# 48. 🧪 End-to-End Example

Suppose:

``` http
POST /login

{
    "username": "sagar",
    "password": "secret"
}
```

Conceptual flow:

``` text
1. Tomcat receives request
        ↓
2. DelegatingFilterProxy
        ↓
3. FilterChainProxy
        ↓
4. Matching SecurityFilterChain selected
        ↓
5. Authentication filter
        ↓
6. UsernamePasswordAuthenticationToken
        ↓
7. AuthenticationManager
        ↓
8. ProviderManager
        ↓
9. DaoAuthenticationProvider
        ↓
10. UserDetailsService
        ↓
11. Repository
        ↓
12. Database
        ↓
13. CustomUserDetails
        ↓
14. PasswordEncoder.matches()
        ↓
15. Account-state checks
        ↓
16. Authenticated Authentication
        ↓
17. SecurityContext
        ↓
18. Continue request / establish authenticated state
```

------------------------------------------------------------------------

# 49. 🧠 Why So Many Layers?

At first this architecture looks over-engineered.

But each layer has a different responsibility.

Imagine one giant class:

``` java
login(request)
```

doing everything:

``` text
parse HTTP
load DB
check password
handle JWT
handle OTP
check LDAP
create authentication
store session
authorize request
return HTTP response
```

That would be hard to extend.

Spring Security instead separates mechanisms:

``` text
HTTP extraction
      ↓
Authentication abstraction
      ↓
AuthenticationManager
      ↓
Provider
      ↓
User loading / credential validation
      ↓
SecurityContext
      ↓
Authorization
```

Now adding a new authentication mechanism does not require rewriting the
entire framework.

------------------------------------------------------------------------

# 50. 🔌 AuthenticationProvider = Strategy

`AuthenticationProvider` is effectively a strategy abstraction.

Different providers implement different authentication mechanisms:

``` text
Username/password → DaoAuthenticationProvider
JWT               → JwtAuthenticationProvider
LDAP               → LDAP provider
Custom OTP         → Custom AuthenticationProvider
```

The manager can choose the appropriate strategy.

This is one of the places where the **Strategy pattern** appears
naturally in Spring Security.

------------------------------------------------------------------------

# 51. 🔁 Stateful Authentication Lifecycle

A simplified stateful flow:

``` text
LOGIN
  ↓
authenticate credentials
  ↓
Authentication
  ↓
SecurityContext
  ↓
session persistence
  ↓
client receives session cookie
```

Next request:

``` text
SESSION COOKIE
      ↓
session state restored
      ↓
SecurityContext
      ↓
Authentication
      ↓
authorization
```

The key:

> Authentication state is reused rather than reconstructing identity
> from raw credentials on every request.

------------------------------------------------------------------------

# 52. 🎫 Stateless Token Lifecycle

Simplified:

``` text
LOGIN
  ↓
authenticate credentials
  ↓
issue token
  ↓
client stores token
```

Next request:

``` text
Authorization: Bearer TOKEN
              ↓
       authentication filter
              ↓
        token validation
              ↓
        Authentication
              ↓
       SecurityContext
              ↓
          controller
```

The `SecurityContext` is still useful during the request even though the
overall application authentication model is stateless.

This is an extremely important distinction:

> **Stateless authentication does not mean there is no
> SecurityContext.**

It means the server does not maintain a persistent authentication
session for the client in the traditional stateful sense.

------------------------------------------------------------------------

# 53. 🔥 Authentication State vs SecurityContext

Do not confuse:

``` text
Stateful authentication
```

with:

``` text
SecurityContext
```

A stateless request can still have:

``` text
SecurityContext
    ↓
Authentication
```

for the duration of request processing.

The context can then disappear after the request.

------------------------------------------------------------------------

# 54. 🧹 SecurityContext Lifecycle

Conceptually:

``` text
Request starts
    ↓
SecurityContext obtained/restored
    ↓
Authentication established
    ↓
Application executes
    ↓
Authorization checks
    ↓
Request finishes
    ↓
Context is cleared/persisted according to configuration
```

Modern Spring Security versions have evolved the exact persistence
behavior, so do not assume every version uses exactly the same
filter/component names.

------------------------------------------------------------------------

# 55. 🧠 What Happens When Authentication Fails?

Example:

``` text
username = sagar
password = wrong
```

Flow:

``` text
Authentication Filter
        ↓
AuthenticationManager
        ↓
ProviderManager
        ↓
DaoAuthenticationProvider
        ↓
UserDetailsService
        ↓
PasswordEncoder.matches()
        ↓
FAIL
        ↓
AuthenticationException
        ↓
failure handling
        ↓
401 / login failure response
```

The controller usually never receives the request as an authenticated
request.

------------------------------------------------------------------------

# 56. 🚫 Authentication vs Authorization Failure

### Case 1

``` text
No valid authentication
```

Result:

``` text
401 Unauthorized
```

### Case 2

``` text
Authenticated user
but insufficient authority
```

Result:

``` text
403 Forbidden
```

Mental shortcut:

``` text
401 → "Who are you?"
403 → "I know who you are, but NO."
```

------------------------------------------------------------------------

# 57. 🧭 Important Concepts Still To Learn

The current understanding is strong, but Spring Security is not complete
yet.

These are the major next topics.

## Authentication

-   [ ] Form login deeply
-   [ ] HTTP Basic deeply
-   [ ] AuthenticationEntryPoint
-   [ ] AuthenticationFailureHandler
-   [ ] AuthenticationSuccessHandler
-   [ ] Session management
-   [ ] Logout
-   [ ] Remember-me authentication
-   [ ] Anonymous authentication
-   [ ] SecurityContext persistence
-   [ ] Custom AuthenticationProvider
-   [ ] Custom AuthenticationManager
-   [ ] AuthenticationManagerResolver

## Authorization

-   [ ] `AuthorizationManager`
-   [ ] Request authorization
-   [ ] Method security
-   [ ] `@PreAuthorize`
-   [ ] `@PostAuthorize`
-   [ ] `@Secured`
-   [ ] Role vs authority semantics
-   [ ] Ownership/resource-level authorization
-   [ ] Authorization decisions internally

## Web Security

-   [ ] CSRF deeply
-   [ ] CORS deeply
-   [ ] Security headers
-   [ ] Session fixation
-   [ ] Session concurrency
-   [ ] Security response headers
-   [ ] Request matching
-   [ ] Filter ordering

## Tokens

-   [ ] JWT structure
-   [ ] JWT signing
-   [ ] JWT validation
-   [ ] Access token
-   [ ] Refresh token
-   [ ] Token expiry
-   [ ] Token rotation
-   [ ] Revocation strategies
-   [ ] Key management
-   [ ] JWK
-   [ ] Opaque token
-   [ ] Token introspection
-   [ ] Bearer token authentication

## OAuth2 / OpenID Connect

-   [ ] OAuth2 roles
-   [ ] Resource Owner
-   [ ] Client
-   [ ] Authorization Server
-   [ ] Resource Server
-   [ ] Access token
-   [ ] Refresh token
-   [ ] Authorization Code flow
-   [ ] PKCE
-   [ ] OpenID Connect
-   [ ] ID Token
-   [ ] UserInfo
-   [ ] Scopes
-   [ ] Claims

## Spring Boot

-   [ ] Security auto-configuration
-   [ ] Default security configuration
-   [ ] Generated default password
-   [ ] Bean creation/wiring
-   [ ] Conditional auto-configuration
-   [ ] Replacing Boot defaults with custom beans

------------------------------------------------------------------------

# 58. 🏗️ Final Mental Model

If everything else becomes blurry, remember this:

``` text
                 HTTP REQUEST
                      │
                      ▼
               Servlet Container
                      │
                      ▼
           DelegatingFilterProxy
                      │
                      ▼
              FilterChainProxy
                      │
                      ▼
             SecurityFilterChain
                      │
              ┌───────┴────────┐
              │                │
       Authentication      Exploit
          Filters          Protection
              │
              ▼
        Authentication
        (request object)
              │
              ▼
      AuthenticationManager
              │
              ▼
        ProviderManager
              │
       ┌──────┴──────┐
       │             │
       ▼             ▼
 Authentication   Authentication
   Provider         Provider
       │
       ▼
DaoAuthenticationProvider
       │
       ├───────────────┐
       ▼               ▼
UserDetailsService  PasswordEncoder
       │
       ▼
 Repository / DB
       │
       ▼
   UserDetails
       │
       ▼
Authenticated Authentication
       │
       ▼
 SecurityContext
       │
       ▼
SecurityContextHolder
       │
       ▼
 AuthorizationManager
       │
   ┌───┴────┐
   │        │
 ALLOW     DENY
   │        │
   ▼        ▼
Controller 403
```

------------------------------------------------------------------------

# 59. 🎯 One-Sentence Definitions

  Concept                     Mental Definition
  --------------------------- ---------------------------------------------------------
  Asset                       Something valuable we protect
  Threat                      Potential source of harm
  Vulnerability               Weakness that can be exploited
  Trust boundary              Point where trust level changes
  Identity                    Who someone claims to be
  Credential                  Evidence used to prove identity
  Authentication              Establishing who someone is
  Authorization               Deciding what they can do
  Principal                   Current authenticated identity
  Authority                   Permission/capability
  Role                        Grouping of authorities
  Authentication              Security representation of authentication state
  SecurityContext             Holds current Authentication
  SecurityContextHolder       Access point for current SecurityContext
  UserDetails                 Spring Security representation of a user
  UserDetailsService          Loads UserDetails
  PasswordEncoder             Verifies/encodes passwords
  AuthenticationManager       Coordinates authentication
  ProviderManager             AuthenticationManager that delegates to providers
  AuthenticationProvider      Performs one authentication mechanism
  DaoAuthenticationProvider   Username/password provider
  SecurityFilterChain         Ordered security filters for matching requests
  FilterChainProxy            Selects/runs the appropriate security chain
  DelegatingFilterProxy       Bridges Servlet container and Spring
  AuthenticationEntryPoint    Handles unauthenticated access
  AccessDeniedHandler         Handles forbidden access
  AuthorizationManager        Makes authorization decisions
  CSRF                        Defense against forged browser requests
  CORS                        Browser cross-origin access control
  Stateful                    Server maintains authentication state
  Stateless                   No traditional per-client server authentication session
  Bearer token                Whoever possesses token can use it
  JWT                         Self-contained signed token format
  Opaque token                Token whose meaning is resolved externally

------------------------------------------------------------------------

# 60. 🧪 Practice Path

Don't just read this.

Build the concepts in increasing complexity:

``` text
1. Spring Security without Boot
        ↓
2. Form Login
        ↓
3. In-memory users
        ↓
4. Custom UserDetails
        ↓
5. UserDetailsService + DB
        ↓
6. PasswordEncoder
        ↓
7. DaoAuthenticationProvider
        ↓
8. Custom AuthenticationProvider
        ↓
9. Request authorization
        ↓
10. Method authorization
        ↓
11. CSRF + CORS
        ↓
12. Session management
        ↓
13. Logout
        ↓
14. JWT
        ↓
15. Refresh tokens
        ↓
16. OAuth2 Resource Server
        ↓
17. OAuth2 Authorization Server concepts
        ↓
18. OpenID Connect
        ↓
19. Microservice security
```

------------------------------------------------------------------------

# 🧠 The Core Question To Ask While Learning

For every Spring Security feature, ask:

``` text
WHO is making the request?
        ↓
How did we establish that identity?
        ↓
Where is the Authentication object created?
        ↓
Who created it?
        ↓
Which filter initiated the process?
        ↓
Which AuthenticationManager received it?
        ↓
Which Provider handled it?
        ↓
How was the user loaded?
        ↓
How were credentials verified?
        ↓
Where is Authentication stored?
        ↓
How does authorization read it?
        ↓
What happens if authentication fails?
        ↓
What happens if authorization fails?
```

If you can trace those questions through the source code, you understand
Spring Security rather than merely knowing how to configure it.

------------------------------------------------------------------------

# 🚀 Current Progress

### ✅ Understood

-   [x] Security fundamentals
-   [x] Assets / threats / vulnerabilities
-   [x] Trust boundaries
-   [x] Identity / credentials / claims
-   [x] Authentication
-   [x] Authentication factors
-   [x] MFA
-   [x] Authorization
-   [x] Principal
-   [x] Authorities
-   [x] Roles
-   [x] Rate limiting concept
-   [x] Stateful authentication
-   [x] Stateless authentication
-   [x] Authentication revocation trade-offs
-   [x] Self-contained tokens
-   [x] Opaque tokens
-   [x] Bearer tokens
-   [x] Authentication interface
-   [x] Authentication request vs authenticated result
-   [x] SecurityContext
-   [x] SecurityContextHolder
-   [x] ThreadLocal mental model
-   [x] Spring Security filter architecture
-   [x] DelegatingFilterProxy
-   [x] FilterChainProxy
-   [x] SecurityFilterChain
-   [x] HttpSecurity builder
-   [x] Authentication filters
-   [x] Form login
-   [x] HTTP Basic
-   [x] AuthenticationManager
-   [x] ProviderManager
-   [x] AuthenticationProvider
-   [x] `supports()`
-   [x] DaoAuthenticationProvider
-   [x] UserDetails
-   [x] Custom UserDetails
-   [x] UserDetailsService
-   [x] PasswordEncoder
-   [x] Authentication failure flow
-   [x] 401 vs 403
-   [x] Basic CSRF concept
-   [x] Basic CORS concept
-   [x] Spring Boot auto-configuration mental model

### 🔜 Next Deep Dive

-   [ ] Trace `UsernamePasswordAuthenticationFilter` internally
-   [ ] Trace `ProviderManager` source flow
-   [ ] Trace `DaoAuthenticationProvider`
-   [ ] Trace `UserDetailsService`
-   [ ] Trace `SecurityContext` lifecycle
-   [ ] Trace authorization internally
-   [ ] Deep CSRF
-   [ ] Deep CORS
-   [ ] Sessions and SecurityContext persistence
-   [ ] JWT from first principles
-   [ ] JWT implementation
-   [ ] Refresh tokens
-   [ ] OAuth2
-   [ ] OpenID Connect
-   [ ] Microservice authentication/authorization

------------------------------------------------------------------------

# 🏁 Final Goal

By the end of this folder, the goal is not:

> "I know how to write `.oauth2ResourceServer()`."

The goal is:

> **I can explain exactly how an HTTP request enters Spring Security,
> how identity is established, how an Authentication object is created,
> how it reaches the SecurityContext, how authorities are represented,
> how authorization is decided, how failures become HTTP responses, and
> how the architecture changes when moving from sessions to JWT/OAuth2
> in a distributed system.**

That is the level of understanding needed to design secure Spring Boot
APIs and later split them cleanly into microservices.
