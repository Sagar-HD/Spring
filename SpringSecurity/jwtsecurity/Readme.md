# 🔐 Spring Security + JWT — First Principles Deep Dive

> **Goal:** Understand Spring Security and JWT from the inside out — not just memorize annotations and configuration.

This README builds one continuous mental model:

```text
HTTP
 ↓
Security Filter Chain
 ↓
Authentication
 ↓
AuthenticationManager
 ↓
AuthenticationProvider
 ↓
SecurityContext
 ↓
Session OR JWT
 ↓
Authorization
 ↓
Controller
```

The central question throughout the document is:

> **Where does the current request's `Authentication` come from?**

## 🧭 Table of Contents

1. The Big Picture
2. Security Problems: Authentication vs Authorization
3. Spring Security Core Architecture
4. Security Filter Chain — Session Version
5. Username/Password Login — Session
6. SecurityContext, ThreadLocal and Session
7. Actual Runtime Flows — Session
8. Why JWT Changes the Architecture
9. What Changes in the SecurityFilterChain for JWT
10. JWT Login Flow
11. JWT Authentication on Every Request
12. Login Authentication vs JWT Request Authentication
13. OncePerRequestFilter
14. Why Add the JWT Filter Before UsernamePasswordAuthenticationFilter
15. JWT From First Principles
16. Base64URL Is Encoding, Not Encryption
17. JWT Signature and Tamper Protection
18. Symmetric vs Asymmetric Signing
19. JWS vs JWE
20. JWT Claims
21. DB Lookup on Every JWT Request
22. Roles and JwtAuthenticationConverter
23. Manual JWT Filter vs Resource Server
24. JWT Encoder and Decoder
25. Spring MVC Controller Arguments
26. Principal vs Authentication vs UserDetails
27. @AuthenticationPrincipal
28. Stateful vs Stateless Comparison
29. Common Misconceptions
30. Practical Implementation Roadmap
31. Final Mental Model
32. Recommended Experiments
33. Final Architecture Map

---

# 1. 🧠 The Big Picture

Spring Security becomes much easier once we stop thinking in terms of annotations and start thinking in terms of **request state**.

Every protected HTTP request eventually needs an:

```text
Authentication
```

inside:

```text
SecurityContext
```

because authorization needs to know:

> **Who is making this request?**

The biggest difference between session authentication and JWT authentication is **where that Authentication comes from**.

### Stateful session authentication

```text
Browser
   │
   │ JSESSIONID
   ▼
Server Session
   │
   └── SecurityContext
          │
          └── Authentication
```

The server remembers authentication state between requests.

### Stateless JWT authentication

```text
Browser
   │
   │ Authorization: Bearer <JWT>
   ▼
JWT authentication mechanism
   │
   ├── verify token
   ├── extract identity/claims
   └── create Authentication
             │
             ▼
        SecurityContext
```

The server reconstructs the request's authentication state from the token.

> 🔑 **Same downstream authorization system. Different source of Authentication.**


# 2. 🛡️ The Security Problems We Are Solving

A web application needs to answer two different questions:

### Authentication — Who are you?

```text
Authentication
```

Examples:

- Username + password
- Session
- JWT
- HTTP Basic
- OAuth2/OIDC

### Authorization — What are you allowed to do?

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/admin/**").hasRole("ADMIN")
    .requestMatchers("/profile").authenticated()
)
```

or:

```java
@PreAuthorize("hasRole('ADMIN')")
```

The architecture is:

```text
Authentication
      ↓
SecurityContext
      ↓
Authorization
      ↓
ALLOW / DENY
```

Authorization does not need to care whether the Authentication came from a session or JWT.


# 3. 🏗️ Spring Security's Core Architecture

At a high level:

```text
HTTP Request
     │
     ▼
Servlet Container
     │
     ▼
Spring Security Filter Chain
     │
     ├── SecurityContext management
     ├── Authentication mechanisms
     ├── Other security filters
     │
     ▼
Authorization
     │
     ▼
DispatcherServlet
     │
     ▼
Controller
```

Spring Security is heavily filter-based. The filters run before your controller, which is why authentication can already exist when your controller executes.

## The chain is built once, then reused

Your `SecurityFilterChain` configuration is used to build the filter chain. It is **not rebuilt differently for every request**.

Every request enters the same configured chain.

What changes is what each filter does:

```text
Same SecurityFilterChain
        │
        ├── login request
        ├── request with JSESSIONID
        └── request with Bearer JWT
```

Each filter decides whether the current request is relevant to its responsibility.

Conceptually:

```text
LogoutFilter
    ↓
Is this /logout?
    ├── YES → handle logout
    └── NO  → continue

UsernamePasswordAuthenticationFilter
    ↓
Is this its login-processing request?
    ├── YES → authenticate
    └── NO  → continue

JWT filter
    ↓
Is there a Bearer token?
    ├── YES → validate and establish Authentication
    └── NO  → continue

AuthorizationFilter
    ↓
Which rule matches?
    ↓
Does current Authentication satisfy it?
```

> ⚠️ The exact filter list and order can vary with Spring Security version and enabled features. The diagrams below show the important filters for the configurations being discussed.


# 4. 🔗 Security Filter Chain — Session Version

For the session/form-login configuration, the relevant filters can conceptually include:

```text
SecurityContextHolderFilter
        ↓
HeaderWriterFilter
        ↓
CorsFilter
        ↓
LogoutFilter
        ↓
UsernamePasswordAuthenticationFilter
        ↓
RequestCacheAwareFilter
        ↓
SecurityContextHolderAwareRequestFilter
        ↓
AnonymousAuthenticationFilter
        ↓
ExceptionTranslationFilter
        ↓
AuthorizationFilter
```

> `CsrfFilter` would normally be part of a web-security setup, but if CSRF is explicitly disabled, it is not active.

These filters are configured by Spring Security from the features enabled through `HttpSecurity`; you normally do not instantiate them manually.

For example:

```java
.formLogin(...)
```

configures form-login infrastructure.

```java
.logout(...)
```

configures logout infrastructure.

```java
.authorizeHttpRequests(...)
```

configures authorization infrastructure.

Then:

```java
http.build();
```

builds the final chain.


# 5. 🔑 Username/Password Login — Session

Suppose:

```http
POST /login
Content-Type: application/x-www-form-urlencoded

username=sagar&password=1234
```

The important authentication flow is:

```text
Username + Password
       │
       ▼
UsernamePasswordAuthenticationFilter
       │
       ▼
UsernamePasswordAuthenticationToken
       │
       ▼
AuthenticationManager
       │
       ▼
DaoAuthenticationProvider
       │
       ├── UserDetailsService → UserRepository → DB
       │
       └── PasswordEncoder
       │
       ▼
Authentication SUCCESS
       │
       ▼
SecurityContext
       │
       ▼
HTTP Session
       │
       ▼
JSESSIONID → Browser
```

`UsernamePasswordAuthenticationFilter` does not itself verify the password against the database. It creates/delegates an authentication request to the `AuthenticationManager`.

## AuthenticationManager

Think of it as the authentication coordinator:

```java
Authentication authenticate(Authentication authentication)
```

Conceptually:

```text
AuthenticationManager
       │
       ├── Provider A
       ├── Provider B
       └── DaoAuthenticationProvider
```

For username/password authentication:

```text
AuthenticationManager
       ↓
DaoAuthenticationProvider
       ↓
UserDetailsService
       ↓
Database
```

## CustomUserDetails

If your domain entity is:

```text
User
├── username
├── password
├── roles
└── ...
```

while Spring Security expects `UserDetails`, a `CustomUserDetails` wrapper is an adapter:

```text
Spring Security
      │
      │ expects UserDetails
      ▼
CustomUserDetails
      │
      ▼
Your User entity
```

`DaoAuthenticationProvider` then uses the configured `PasswordEncoder` to compare the supplied password with the stored hash.


# 6. 📦 SecurityContext, ThreadLocal and Session

After successful authentication:

```text
Authentication
      ↓
SecurityContext
      ↓
SecurityContextHolder
      ↓
ThreadLocal
```

The current context is accessible through:

```java
SecurityContextHolder.getContext()
```

and:

```java
SecurityContextHolder
    .getContext()
    .getAuthentication();
```

### ThreadLocal vs Session

This distinction is critical:

```text
ThreadLocal → current request/thread
Session     → cross-request persistence
```

`ThreadLocal` is not what makes session authentication persistent.

Conceptually, after login:

```text
Authentication
      │
      ├──────────→ SecurityContextHolder → ThreadLocal
      │
      └──────────→ security-context persistence → HttpSession
```

The browser receives:

```http
Set-Cookie: JSESSIONID=ABC123
```

and later sends:

```http
Cookie: JSESSIONID=ABC123
```

The servlet container identifies the corresponding `HttpSession`, and Spring Security's security-context persistence mechanism can load the stored `SecurityContext`.

```text
JSESSIONID
    ↓
HttpSession
    ↓
SecurityContext
    ↓
Authentication
    ↓
SecurityContextHolder
    ↓
ThreadLocal
```

### SessionCreationPolicy

A policy such as:

```java
SessionCreationPolicy.IF_REQUIRED
```

allows Spring Security to use/create sessions when required.

Keep these concepts separate:

```text
SessionCreationPolicy
    ↓
How sessions may be used/created

SecurityContextRepository
    ↓
Where the SecurityContext is loaded/saved
```


# 7. 🔄 Actual Runtime Flows — Session

## 7.1 Login request

```text
POST /login
     │
     ▼
SecurityFilterChain
     │
     ▼
SecurityContextHolderFilter
     │
     ▼
HeaderWriterFilter
     │
     ▼
CorsFilter
     │
     ▼
LogoutFilter
     │
     │ not /logout → continue
     ▼
UsernamePasswordAuthenticationFilter
     │
     │ matches login-processing request
     ▼
Authenticate
     │
     ▼
AuthenticationManager
     │
     ▼
DaoAuthenticationProvider
     │
     ├── UserDetailsService → DB
     └── PasswordEncoder
     │
     ▼
Authentication SUCCESS
     │
     ▼
SecurityContext/session persistence
     │
     ▼
HTTP Session
     │
     ▼
JSESSIONID
```

A successful form-login request is handled by the authentication filter's success handling; do not think of it as an ordinary protected request that simply continues through authorization to your application controller.

## 7.2 Subsequent request with JSESSIONID

```text
GET /profile
Cookie: JSESSIONID=ABC123
     │
     ▼
SecurityFilterChain
     │
     ▼
SecurityContextHolderFilter
     │
     ▼
Load SecurityContext from session
     │
     ▼
Authentication
     │
     ▼
HeaderWriterFilter
     ↓
CorsFilter
     ↓
LogoutFilter
     │
     │ not /logout
     ▼
UsernamePasswordAuthenticationFilter
     │
     │ not /login → continue
     ▼
RequestCacheAwareFilter
     ↓
SecurityContextHolderAwareRequestFilter
     ↓
AnonymousAuthenticationFilter
     │
     │ Authentication already exists → no replacement
     ▼
ExceptionTranslationFilter
     ↓
AuthorizationFilter
     ↓
Authorization rules
     ↓
Controller
```

## 7.3 Protected request without JSESSIONID

```text
GET /profile
     │
     ▼
SecurityFilterChain
     │
     ▼
SecurityContextHolderFilter
     │
     ▼
No existing session context
     │
     ▼
...
     ↓
AnonymousAuthenticationFilter
     ↓
Anonymous Authentication
     ↓
ExceptionTranslationFilter
     ↓
AuthorizationFilter
     ↓
/profile requires USER or ADMIN
     ↓
DENIED
     ↓
401
```

## 7.4 Login can have a JSESSIONID

A login request may already carry a session cookie:

```http
POST /login
Cookie: JSESSIONID=ABC123
```

That does **not** make it a normal authenticated request.

The request path/matching determines whether the login authentication mechanism handles it.

So:

```text
JSESSIONID exists
    ≠
This is a login request
```

and:

```text
POST /login
    →
login-processing mechanism
```

This distinction becomes very important when adding JWT.


# 8. 🚀 Why JWT Changes the Architecture

JWT changes **how authentication is established between requests**, not what Spring Security ultimately wants.

### Session

```text
JSESSIONID
   ↓
server-side session
   ↓
SecurityContext
   ↓
Authentication
```

### JWT

```text
Authorization: Bearer <JWT>
   ↓
JWT authentication mechanism
   ↓
validate token
   ↓
create Authentication
   ↓
SecurityContext
```

The key insight:

> **You are not replacing `Authentication`, `SecurityContext`, or authorization. You are replacing the mechanism that restores/establishes Authentication between requests.**


# 9. 🔧 What Changes in the SecurityFilterChain for JWT

For a custom learning implementation, your configuration moves away from form-login/session authentication.

Conceptually:

```java
http
    .cors(Customizer.withDefaults())
    .csrf(csrf -> csrf.disable())

    .sessionManagement(session ->
        session.sessionCreationPolicy(
            SessionCreationPolicy.STATELESS
        )
    )

    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/public/**").permitAll()
        .requestMatchers("/auth/login").permitAll()
        .requestMatchers("/auth/register").permitAll()
        .requestMatchers("/admin/**").hasRole("ADMIN")
        .requestMatchers("/profile/**")
            .hasAnyRole("USER", "ADMIN")
        .anyRequest().authenticated()
    )

    .addFilterBefore(
        jwtAuthenticationFilter,
        UsernamePasswordAuthenticationFilter.class
    );
```

### Main changes

#### Remove/stop using form login

```java
.formLogin(...)
```

is no longer the mechanism for your `/auth/login` endpoint.

#### Use stateless session management

```java
.sessionManagement(session ->
    session.sessionCreationPolicy(
        SessionCreationPolicy.STATELESS
    )
)
```

This tells Spring Security not to rely on HTTP session as the authentication mechanism.

#### Add JWT authentication

```java
.addFilterBefore(
    jwtAuthenticationFilter,
    UsernamePasswordAuthenticationFilter.class
)
```

The exact position is a design/configuration choice, but the common purpose is to let JWT authentication establish the `SecurityContext` early enough for downstream authorization.

### What stays

Your authorization rules can remain essentially the same:

```java
.requestMatchers("/admin/**").hasRole("ADMIN")
.requestMatchers("/profile/**")
    .hasAnyRole("USER", "ADMIN")
.anyRequest().authenticated()
```

Why?

Because `AuthorizationFilter` only cares about the resulting:

```text
SecurityContext
      ↓
Authentication
      ↓
Authorities
```

It does not care whether Authentication came from a session or JWT.

### What happens to the filters?

The general security filters remain. The important change is that the **form-login/session authentication path is no longer your authentication mechanism**, and your JWT authentication filter is added.

Conceptually:

```text
SESSION VERSION

SecurityContextHolderFilter
        ↓
HeaderWriterFilter
        ↓
CorsFilter
        ↓
LogoutFilter
        ↓
UsernamePasswordAuthenticationFilter
        ↓
...
        ↓
AnonymousAuthenticationFilter
        ↓
ExceptionTranslationFilter
        ↓
AuthorizationFilter
```

becomes approximately:

```text
JWT VERSION

SecurityContextHolderFilter
        ↓
HeaderWriterFilter
        ↓
CorsFilter
        ↓
JwtAuthenticationFilter ⭐
        ↓
RequestCacheAwareFilter
        ↓
SecurityContextHolderAwareRequestFilter
        ↓
AnonymousAuthenticationFilter
        ↓
ExceptionTranslationFilter
        ↓
AuthorizationFilter
```

> ⚠️ Exact filters/order depend on the Spring Security version and enabled features. The key architectural change is **session/form-login authentication → JWT authentication**.

Also, `CsrfFilter` is absent in these examples because the configuration explicitly disables CSRF.

`LogoutFilter` is not required for JWT authentication in the same way it is for session logout. JWT logout/revocation is a separate design problem; do not assume that clearing a server session invalidates an already-issued JWT.


# 10. 🔑 JWT Login Flow

JWT does **not** replace username/password authentication at login.

The login still authenticates the credentials:

```text
POST /auth/login
       │
       ▼
AuthController
       │
       ▼
UsernamePasswordAuthenticationToken
       │
       ▼
AuthenticationManager
       │
       ▼
DaoAuthenticationProvider
       │
       ├── UserDetailsService → DB
       └── PasswordEncoder
       │
       ▼
Authenticated Authentication
       │
       ▼
Generate JWT
       │
       ▼
JwtEncoder / JWT service
       │
       ▼
Signed JWT
       │
       ▼
Return token to client
```

Example conceptually:

```java
Authentication authentication =
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.username(),
            request.password()
        )
    );

String jwt = jwtService.generateToken(authentication);

return ResponseEntity.ok(jwt);
```

The important separation is:

> **Login authentication checks username/password. JWT verification checks a previously issued token.**

These are different authentication events.

## Why registration is still separate

Your registration flow can remain a normal application service operation:

```text
POST /auth/register
      ↓
UserService
      ↓
encode password
      ↓
save User
```

Registration creates the account.

Login proves the credentials and issues the JWT.

```text
REGISTER
   ↓
create user

LOGIN
   ↓
authenticate user
   ↓
issue JWT
```


# 11. 🔄 JWT Authentication on Every Request

After login, the client sends:

```http
GET /profile
Authorization: Bearer eyJ...
```

The JWT filter performs:

```text
HTTP Request
      │
      ▼
Authorization header
      │
      ▼
Bearer token
      │
      ▼
JWT validation
      │
      ├── signature valid?
      ├── expiration valid?
      ├── issuer valid?
      └── audience valid? (if configured)
      │
      ▼
JWT claims
      │
      ▼
Authentication
      │
      ▼
SecurityContext
      │
      ▼
AuthorizationFilter
      │
      ▼
Controller
```

The password is not checked again.

## Actual JWT filter-chain flow

```text
                         HTTP REQUEST
                              │
                              ▼
                    SecurityFilterChain
                              │
                              ▼
               SecurityContextHolderFilter
                              │
                              ▼
                    No HTTP Session
                              │
                              ▼
                       CorsFilter
                              │
                              ▼
                 JwtAuthenticationFilter ⭐
                              │
                    ┌─────────┴─────────┐
                    │                   │
              JWT present          JWT absent
                    │                   │
                    ▼                   ▼
             Extract JWT           Continue
                    │
                    ▼
             Validate JWT
                    │
                    ▼
          Create Authentication
                    │
                    ▼
            SecurityContext
                    │
                    └──────────────┐
                                   ▼
              SecurityContextHolderAwareRequestFilter
                                   │
                                   ▼
                AnonymousAuthenticationFilter
                                   │
                                   ▼
                 ExceptionTranslationFilter
                                   │
                                   ▼
                    AuthorizationFilter
                                   │
                                   ▼
                         Authorization rules
                                   │
                         ┌─────────┴─────────┐
                         ▼                   ▼
                       ALLOW               DENY
                         │                   │
                         ▼                   ▼
                    Controller            401/403
```

If the JWT is absent, the JWT filter normally just continues. If the endpoint requires authentication, the later authorization machinery rejects the request.

## Stateless does not mean no SecurityContext

During each request:

```text
JWT
 ↓
Authentication
 ↓
SecurityContext
 ↓
request finishes
 ↓
request-local security state is cleared
```

The next request starts again:

```text
JWT
 ↓
NEW Authentication
 ↓
NEW SecurityContext
```

So:

> **Stateless means the server does not persist authentication state across requests in an HTTP session. It does not mean there is no SecurityContext during a request.**


# 12. ⚔️ Login Authentication vs JWT Request Authentication

These are two different flows.

## Login

```text
username + password
        ↓
AuthenticationManager
        ↓
DaoAuthenticationProvider
        ↓
UserDetailsService + PasswordEncoder
        ↓
Authentication
        ↓
generate JWT
```

## Protected request

```text
Bearer JWT
    ↓
JwtAuthenticationFilter
    ↓
validate JWT
    ↓
Authentication
    ↓
SecurityContext
    ↓
Authorization
```

### `UsernamePasswordAuthenticationFilter`

This is Spring Security's standard form-login authentication mechanism.

It handles a configured username/password login-processing request.

### Your custom JWT filter

A custom filter commonly handles:

```http
Authorization: Bearer <JWT>
```

and establishes the current request's `Authentication`.

### They can be conceptually positioned in the same chain

```text
... 
 ↓
JwtAuthenticationFilter
 ↓
UsernamePasswordAuthenticationFilter
 ↓
...
 ↓
AuthorizationFilter
```

But they do not mean:

```text
JWT authentication
    ↓
THEN
username/password authentication
```

They respond to different request shapes.

For:

```text
GET /profile
Authorization: Bearer ...
```

the JWT mechanism handles authentication.

For:

```text
POST /auth/login
username + password
```

your login flow handles credential authentication.

## What if `/login` already has a JWT?

A request can have both:

```http
POST /auth/login
Authorization: Bearer old-token
```

The presence of a Bearer token does not inherently mean `/login` should be authenticated by that token.

A custom JWT filter should define its intended request scope. It may skip the login endpoint so login means:

```text
username + password
    ↓
authenticate
    ↓
issue NEW token
```

rather than:

```text
old JWT
    ↓
already authenticated
    ↓
login again
```

This gives a useful distinction:

```text
addFilterBefore(...)
    ↓
where the filter sits in the chain

filter request matching
    ↓
which requests the filter actually processes
```


# 13. 🔁 OncePerRequestFilter

A custom JWT filter is commonly implemented by extending:

```java
OncePerRequestFilter
```

Conceptually:

```java
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) {

        // inspect Authorization header
        // extract Bearer token
        // validate JWT
        // create Authentication
        // put it into SecurityContext

        filterChain.doFilter(request, response);
    }
}
```

Its responsibility is roughly:

```text
Request
  ↓
Find Bearer token
  ↓
Validate
  ↓
Create Authentication
  ↓
SecurityContextHolder
  ↓
continue chain
```

It should not become a second username/password authentication system.


# 14. ⬆️ Why Add the JWT Filter Before UsernamePasswordAuthenticationFilter

A common custom configuration is:

```java
http.addFilterBefore(
    jwtAuthenticationFilter,
    UsernamePasswordAuthenticationFilter.class
);
```

Conceptually:

```text
...
 ↓
JwtAuthenticationFilter
 ↓
UsernamePasswordAuthenticationFilter
 ↓
...
 ↓
AuthorizationFilter
```

The goal is to let JWT authentication establish the `SecurityContext` early enough for downstream security processing.

Do not interpret this as:

```text
JWT must authenticate
    ↓
then username/password must authenticate
```

Instead:

```text
Bearer request
    ↓
JWT filter handles it

Login request
    ↓
username/password login mechanism handles it
```


# 15. 🔏 JWT From First Principles

A JWT compact representation looks like:

```text
HEADER.PAYLOAD.SIGNATURE
```

Example:

```text
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9
.
eyJzdWIiOiJzYWdhciIsInJvbGUiOiJST0xFX1VTRVIiLCJpYXQiOjE3...
.
signature...
```

## Header

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

## Payload

```json
{
  "sub": "sagar",
  "role": "ROLE_USER",
  "iat": 1724000000,
  "exp": 1724003600
}
```

## Signature

A cryptographic signature over the encoded header and payload.


# 16. 🔓 Base64URL Is Encoding, Not Encryption

JWT header and payload are generally Base64URL encoded.

```text
JSON
  ↓
UTF-8 bytes
  ↓
Base64URL
  ↓
JWT component
```

Base64URL is reversible:

```text
❌ no secret key
❌ no confidentiality
❌ no protection from reading
```

Anyone holding the JWT can decode the header and payload.

> **Never treat an ordinary signed JWT payload as encrypted data.**

Do not put passwords or private secrets in it merely because it is signed.


# 17. ✍️ JWT Signature and Tamper Protection

For a signed JWT, the signing input is conceptually:

```text
Base64URL(header) + "." + Base64URL(payload)
```

Then a signature is produced:

```text
Signing Input
     │
     ▼
Sign(SigningInput, Key)
     │
     ▼
Signature
```

Final token:

```text
Base64URL(header)
.
Base64URL(payload)
.
Base64URL(signature)
```

For HS256:

```text
HMAC-SHA256(
    Base64URL(header) + "." + Base64URL(payload),
    secret
)
```

A JWT signature is not simply "a hash of the payload."

If an attacker changes:

```json
"role": "ROLE_USER"
```

to:

```json
"role": "ROLE_ADMIN"
```

the original signature no longer matches the modified signing input.

> 🔑 **The algorithm does not need to be secret. The key does.**


# 18. 🔑 Symmetric vs Asymmetric Signing

## Symmetric signing

Example:

```text
HS256
```

The same secret signs and verifies:

```text
             SECRET
            /                 ▼        ▼
         SIGN     VERIFY
```

Every verifier therefore needs access to the shared secret.

## Asymmetric signing

Examples:

```text
RS256
ES256
```

Private key signs; public key verifies:

```text
PRIVATE KEY
     │
     ▼
   SIGN
     │
     ▼
    JWT
     │
     ▼
PUBLIC KEY
     │
     ▼
  VERIFY
```

This is useful in distributed systems because verifying services do not need the private signing key.

> Be precise: symmetric/asymmetric **signing** is different from encryption.


# 19. 🔐 JWS vs JWE

### JWS — signed

Provides:

```text
Integrity
+
Authenticity of signer
```

The payload remains readable.

### JWE — encrypted

Provides:

```text
Confidentiality
```

So:

```text
JWS → signed
JWE → encrypted
```

You do not need JWE merely to prevent payload tampering. A signature already provides tamper detection.


# 20. 📋 JWT Claims

Common claims:

```text
sub → subject / identity
iat → issued at
exp → expiration
iss → issuer
aud → audience
```

Example:

```json
{
  "sub": "sagar",
  "role": "ROLE_USER",
  "iat": 1724000000,
  "exp": 1724003600
}
```

The server should validate relevant registered claims such as expiration and, when applicable, issuer and audience.

Custom claims can represent application-specific information such as roles.


# 21. 🗄️ DB Lookup on Every JWT Request

A JWT implementation may perform a database lookup on every request:

```text
JWT
 ↓
extract username
 ↓
UserDetailsService
 ↓
Database
 ↓
UserDetails
 ↓
Authentication
```

Therefore:

```text
Request 1 → DB
Request 2 → DB
Request 3 → DB
```

This can still be stateless.

> Stateless does not mean "never touch the database." It means authentication state is not persisted in a server-side session between requests.

Alternatively, trusted claims can contain enough authority information:

```json
{
  "sub": "sagar",
  "role": "ROLE_USER",
  "exp": 1724003600
}
```

Then:

```text
JWT
 ↓
verify signature
 ↓
extract claims
 ↓
Authentication
```

No user lookup may be necessary.

Tradeoff: already-issued tokens may continue to carry old roles until expiration or another invalidation mechanism is used.


# 22. 🎭 Roles and JwtAuthenticationConverter

When JWT claims contain roles/authorities, Spring needs a mapping from claims to `GrantedAuthority`.

Conceptually:

```text
JWT claim
"role": "ROLE_USER"
        ↓
JwtAuthenticationConverter
        ↓
GrantedAuthority
ROLE_USER
        ↓
Authentication
```

This lets your application's authority model work with authorization rules such as:

```java
.hasRole("USER")
```

The exact claim-to-authority convention depends on the configured mechanism, so explicit conversion is useful when your token structure differs from defaults.


# 23. 🧰 Manual JWT Filter vs Resource Server

## Custom filter — useful for learning

```text
OncePerRequestFilter
       ↓
extract Bearer token
       ↓
JwtDecoder
       ↓
validate
       ↓
create Authentication
       ↓
SecurityContext
```

This exposes the mechanics clearly.

## Spring Security Resource Server — production-oriented abstraction

Conceptually:

```text
HTTP Request
    ↓
BearerTokenAuthenticationFilter
    ↓
JwtDecoder
    ↓
JwtAuthenticationProvider
    ↓
JwtAuthenticationConverter
    ↓
Authentication
    ↓
SecurityContext
```

Learning strategy:

1. Build/understand the custom version once.
2. Understand the Resource Server version.
3. Prefer the framework-provided security infrastructure in production rather than reinventing it.


# 24. 🔏 JWT Encoder and Decoder

Conceptually:

```text
JWT signing key / keys
        │
        ├──────────→ JwtEncoder
        │                 ↓
        │               SIGN
        │
        └──────────→ JwtDecoder
                          ↓
                        VERIFY
```

### JwtEncoder

Used when your application creates the token:

```text
Claims
  ↓
JwtEncoder
  ↓
Signed JWT
```

### JwtDecoder

Used when your application receives a JWT:

```text
JWT
  ↓
JwtDecoder
  ↓
Validated claims
```

With asymmetric signing:

```text
Private key → encoder/signing
Public key  → decoder/verification
```

With symmetric signing:

```text
Shared secret → encoder
Shared secret → decoder
```


# 25. 🎯 Spring MVC Controller Arguments

You may see:

```java
@GetMapping("/profile")
public Profile profile(Authentication authentication) {
    ...
}
```

This is not ordinary constructor dependency injection. Spring MVC uses method-argument resolution.

Conceptually:

```text
DispatcherServlet
      ↓
Find controller method
      ↓
HandlerMethodArgumentResolver
      ↓
SecurityContext
      ↓
Authentication
      ↓
invoke controller
```

So the controller can receive the current `Authentication` because Spring Security established it before the controller runs.


# 26. 👤 Principal vs Authentication vs UserDetails

### Principal

```java
Principal principal
```

Represents the current caller identity.

```java
principal.getName();
```

### Authentication

Contains more information:

```text
Authentication
 ├── principal
 ├── credentials
 ├── authorities
 └── authenticated
```

### UserDetails

Spring Security's security-related user representation used by mechanisms such as `DaoAuthenticationProvider`.

Relationship:

```text
SecurityContext
      ↓
Authentication
      ↓
principal
      ↓
UserDetails
```

Easy memory trick:

> 👤 **Principal = WHO**

> 🪪 **Authentication = WHO + authentication information**

> 📦 **SecurityContext = WHERE the current Authentication is held**

> 👨‍💻 **UserDetails = security-related representation of the user**


# 27. 🎯 @AuthenticationPrincipal

Example:

```java
@GetMapping("/profile")
public Profile profile(
    @AuthenticationPrincipal UserDetails user
) {
    ...
}
```

Conceptually:

```text
SecurityContextHolder
      ↓
SecurityContext
      ↓
Authentication
      ↓
authentication.getPrincipal()
      ↓
UserDetails
      ↓
controller argument
```

It means:

> Give me the principal from the current Authentication.

It does not independently authenticate the user.


# 28. ⚖️ Complete Stateful vs Stateless Comparison

| Concept | Session | JWT |
|---|---|---|
| Login credentials | Username/password | Username/password |
| Initial authentication | DaoAuthenticationProvider | DaoAuthenticationProvider |
| Result | Authentication | Authentication |
| Persistent auth state | Server-side session | No server-side session required |
| Client sends | JSESSIONID | Bearer JWT |
| Next request identity | Restore SecurityContext | Validate token + create Authentication |
| SecurityContext during request | Yes | Yes |
| ThreadLocal during request | Yes | Yes |
| Server remembers auth between requests | Yes | No |
| Token contains claims | No | Yes |
| Signature verification | No JWT signature | Yes |
| Immediate revocation | Easier | Harder for already-issued tokens |
| Horizontal scaling | Session sharing/stickiness may matter | Easier from auth-state perspective |
| Main credential theft concern | Session ID theft | JWT theft |
| Confidentiality of ordinary token | N/A | Payload is readable |
| Tamper protection | Server-side state | Cryptographic signature |


# 29. 🚨 Common Misconceptions

### ❌ "Base64 encrypts the JWT"

No.

```text
Base64URL = encoding
```

Anyone can decode it.

### ❌ "JWT signature encrypts the payload"

No.

```text
Signature → integrity/authenticity
Encryption → confidentiality
```

### ❌ "Signature is just a hash"

Not generally. The cryptographic operation depends on the algorithm.

### ❌ "If the attacker knows HS256, JWT is broken"

No. The algorithm is public; the secret key must remain secret.

### ❌ "Stateless means no SecurityContext"

No. There is a SecurityContext during the current request.

### ❌ "ThreadLocal makes sessions stateful"

No. ThreadLocal provides current-request access; the session provides cross-request persistence.

### ❌ "DaoAuthenticationProvider authenticates every JWT request"

Not necessarily.

Login:

```text
username/password
    ↓
DaoAuthenticationProvider
```

JWT request:

```text
JWT
 ↓
JWT validation mechanism
 ↓
Authentication
```

### ❌ "JWT replaces Authentication"

No.

JWT is a credential/token format. Spring Security ultimately wants:

```text
SecurityContext
    ↓
Authentication
```

### ❌ "A valid JWT cannot be replayed"

A stolen valid JWT can generally be replayed until it expires or is otherwise invalidated. Signature verification protects against forgery/tampering, not necessarily token theft.


# 30. 🛠️ Practical Implementation Roadmap

## Phase 1 — Registration

```text
POST /auth/register
      ↓
UserService
      ↓
PasswordEncoder
      ↓
UserRepository
      ↓
Database
```

Registration creates the account.

## Phase 2 — Login authentication

```text
POST /auth/login
      ↓
username/password
      ↓
UsernamePasswordAuthenticationToken
      ↓
AuthenticationManager
      ↓
DaoAuthenticationProvider
      ↓
UserDetailsService + PasswordEncoder
      ↓
Authentication
```

## Phase 3 — JWT issuance

Configure:

```text
signing key
issuer
expiration
algorithm
```

Keep secrets/keys outside source code.

Then:

```text
Authentication
      ↓
claims
      ↓
JwtEncoder / JWT service
      ↓
signed JWT
      ↓
client
```

## Phase 4 — JWT validation

Configure:

```text
JwtDecoder
```

Validate relevant properties:

```text
signature
expiration
issuer
audience (when applicable)
```

## Phase 5 — JWT request authentication

Learning version:

```java
OncePerRequestFilter
```

Conceptually:

```text
Request
  ↓
Is login endpoint?
  ├── yes → continue to login handling
  └── no
       ↓
Authorization header?
       ├── no → continue
       └── yes
            ↓
        Bearer token
            ↓
        JwtDecoder
            ↓
        validate
            ↓
        extract claims
            ↓
        create Authentication
            ↓
        SecurityContextHolder
            ↓
        filterChain.doFilter(...)
```

## Phase 6 — Stateless session policy

```java
SessionCreationPolicy.STATELESS
```

Goal:

```text
No authentication session persistence
```

Every protected request must carry its authentication proof.

## Phase 7 — Authorization

Existing rules continue to work:

```java
.requestMatchers("/public/**").permitAll()
.requestMatchers("/admin/**").hasRole("ADMIN")
.requestMatchers("/profile/**")
    .hasAnyRole("USER", "ADMIN")
.anyRequest().authenticated()
```

and:

```java
@PreAuthorize("hasRole('ADMIN')")
```

The authorization layer sees only:

```text
SecurityContext
      ↓
Authentication
      ↓
Authorities
```

It does not need to care whether the Authentication came from session, JWT, HTTP Basic, or OAuth2.


# 31. 🧠 Final Mental Model

## Stateful Session Authentication

```text
                         LOGIN
                           │
                           ▼
                    username/password
                           │
                           ▼
             UsernamePasswordAuthenticationFilter
                           │
                           ▼
                  AuthenticationManager
                           │
                           ▼
                DaoAuthenticationProvider
                           │
                    ┌──────┴──────┐
                    ▼             ▼
             UserDetailsService  PasswordEncoder
                    │
                    ▼
                   DB
                    │
                    ▼
              Authentication
                    │
                    ▼
              SecurityContext
                    │
              ┌─────┴─────┐
              ▼           ▼
       SecurityContext   Session
          Holder            │
              │             ▼
              ▼        HttpSession
          ThreadLocal       │
                            ▼
                        JSESSIONID

                  NEXT REQUEST
                       │
                       ▼
                   JSESSIONID
                       │
                       ▼
                   HttpSession
                       │
                       ▼
                 SecurityContext
                       │
                       ▼
                 Authentication
                       │
                       ▼
                 Authorization
                       │
                       ▼
                   Controller
```

## Stateless JWT Authentication

```text
                         LOGIN
                           │
                           ▼
                    username/password
                           │
                           ▼
                  AuthenticationManager
                           │
                           ▼
                DaoAuthenticationProvider
                           │
                    ┌──────┴──────┐
                    ▼             ▼
             UserDetailsService  PasswordEncoder
                    │
                    ▼
                   DB
                    │
                    ▼
              Authentication
                    │
                    ▼
                 JwtEncoder
                    │
                    ▼
           HEADER.PAYLOAD.SIGNATURE
                    │
                    ▼
                  CLIENT


                  NEXT REQUEST
                       │
                       ▼
             Authorization: Bearer <JWT>
                       │
                       ▼
             JwtAuthenticationFilter
                       │
                       ▼
                  JwtDecoder
                       │
              ┌────────┴────────┐
              ▼                 ▼
       Verify signature    Validate claims
              │                 │
              └────────┬────────┘
                       ▼
                  JWT Claims
                       │
                       ▼
                 Authentication
                       │
                       ▼
                 SecurityContext
                       │
                       ▼
                 Authorization
                       │
                       ▼
                  Controller
                       │
                       ▼
              Request-local state ends
```

## The transition in one picture

```text
SESSION WORLD

JSESSIONID
    ↓
Session
    ↓
SecurityContext
    ↓
Authentication
    ↓
Authorization


JWT WORLD

Bearer JWT
    ↓
JWT Filter
    ↓
validate
    ↓
Authentication
    ↓
SecurityContext
    ↓
Authorization
```

The critical insight is:

> **Session authentication retrieves an existing Authentication from server-side state. JWT authentication establishes a new Authentication from the token on each request.**

And therefore:

```text
Authentication
      ↓
SecurityContext
      ↓
Authorization
      ↓
Controller
```

is the common downstream path.

Everything before `Authentication` is the part that changes between authentication mechanisms.


# 32. 🧪 Recommended Experiments

### Experiment 1 — Session

Login and inspect:

```text
JSESSIONID
SecurityContext
Authentication
```

Make a second request and observe that authentication is restored.

### Experiment 2 — ThreadLocal

Inside a controller/service:

```java
SecurityContextHolder
    .getContext()
    .getAuthentication();
```

Observe the current Authentication.

### Experiment 3 — JWT decoding

Decode only:

```text
header
payload
```

Observe that they are readable without the signing secret.

### Experiment 4 — Tampering

Change:

```text
ROLE_USER
```

to:

```text
ROLE_ADMIN
```

without resigning the token.

Observe signature validation failure.

### Experiment 5 — Token expiration

Use a short expiration and observe the decoder reject the expired token.

### Experiment 6 — Stateless mode

Enable:

```java
SessionCreationPolicy.STATELESS
```

and verify that authentication is no longer restored from an HTTP session.

### Experiment 7 — JWT request

Send:

```http
Authorization: Bearer <token>
```

and inspect:

```text
JWT
 ↓
Authentication
 ↓
SecurityContext
 ↓
Authorization
 ↓
Controller
```

### Experiment 8 — Login vs JWT filter

Log inside your JWT filter and login endpoint.

Test:

```text
POST /auth/login
```

and:

```text
GET /profile
Authorization: Bearer <token>
```

Observe that the same filter chain is entered, but different authentication mechanisms handle the requests.


# 33. 🧩 Final Architecture Map

```text
                     ┌───────────────────────┐
                     │       CLIENT          │
                     └───────────┬───────────┘
                                 │
                       ┌─────────┴─────────┐
                       │                   │
                    REGISTER              LOGIN
                       │                   │
                       ▼                   ▼
                  UserService       username/password
                       │                   │
                       ▼                   ▼
                       DB          AuthenticationManager
                                           │
                                           ▼
                                 DaoAuthenticationProvider
                                           │
                                  ┌────────┴────────┐
                                  ▼                 ▼
                           UserDetailsService  PasswordEncoder
                                  │
                                  ▼
                                 DB
                                  │
                                  ▼
                           Authentication
                                  │
                         ┌────────┴────────┐
                         │                 │
                    Session app       JWT app
                         │                 │
                         ▼                 ▼
                    HttpSession       JwtEncoder
                         │                 │
                         ▼                 ▼
                    JSESSIONID            JWT
                         │                 │
                         │                 │
                         └────────┬────────┘
                                  ▼
                         NEXT API REQUEST
                                  │
                         ┌────────┴────────┐
                         │                 │
                    JSESSIONID       Bearer JWT
                         │                 │
                         ▼                 ▼
                    Session          Jwt Filter
                         │                 │
                         ▼                 ▼
                  SecurityContext    validate token
                         │                 │
                         │                 ▼
                         │            Authentication
                         │                 │
                         └────────┬────────┘
                                  ▼
                         SecurityContext
                                  │
                                  ▼
                            Authorization
                                  │
                                  ▼
                         DispatcherServlet
                                  │
                                  ▼
                             Controller
                                  │
                     ┌────────────┼────────────┐
                     ▼            ▼            ▼
              Authentication  Principal  @AuthenticationPrincipal
```

## The progression

```text
HTTP
 ↓
Servlet Filter
 ↓
Spring Security Filter Chain
 ↓
Authentication
 ↓
AuthenticationManager
 ↓
AuthenticationProvider
 ↓
DaoAuthenticationProvider
 ↓
UserDetailsService + PasswordEncoder
 ↓
SecurityContext
 ↓
SecurityContextHolder / ThreadLocal
 ↓
       ┌───────────────┐
       │               │
       ▼               ▼
    Session           JWT
       │               │
 JSESSIONID        Bearer token
       │               │
       ▼               ▼
restore context   verify + create Authentication
       │               │
       └───────┬───────┘
               ▼
         Authorization
               ↓
          Controller
```

> 🚀 **The goal is not to memorize Spring Security APIs. The goal is to understand what state exists, where it lives, who creates it, who restores it, and how a request proves its identity. Once that is clear, the APIs become implementation details.**

## 🔐 JWT Security Fundamentals

JWT is a compact token representation:

```text
HEADER.PAYLOAD.SIGNATURE
```

### Header

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

### Payload

```json
{
  "sub": "sagar",
  "role": "ROLE_USER",
  "iat": 1724000000,
  "exp": 1724003600
}
```

### Signature

A cryptographic signature over the encoded header and payload.

A signed JWT gives integrity/authenticity of the signer; it does not automatically provide confidentiality.

### Registered claims

```text
sub → subject / identity
iat → issued at
exp → expiration
iss → issuer
aud → audience
```

The server should validate the claims relevant to its trust model.

---

## 🧠 JWT Security Model

The important property of a signed JWT is:

```text
Header.Payload
      +
Signing Key
      ↓
Signature
```

If the payload is modified:

```text
"role": "ROLE_USER"
```

to:

```text
"role": "ROLE_ADMIN"
```

the original signature no longer matches.

> 🔑 **The algorithm can be public. The signing key must be protected.**

### Symmetric

```text
          SECRET
         /            SIGN      VERIFY
```

Example: `HS256`.

### Asymmetric

```text
PRIVATE KEY → SIGN
PUBLIC KEY  → VERIFY
```

Examples: `RS256`, `ES256`.

Asymmetric signing is useful when one service signs while many services only need to verify.

---

## 🔐 JWS vs JWE

```text
JWS → signed → integrity/authenticity
JWE → encrypted → confidentiality
```

A normal signed JWT payload is readable. Do not place passwords or private secrets in it merely because it is signed.

---

## 🧰 Security Infrastructure That Remains Common

Infrastructure filters such as these may participate depending on configuration/version:

```text
HeaderWriterFilter
CorsFilter
RequestCacheAwareFilter
SecurityContextHolderAwareRequestFilter
AnonymousAuthenticationFilter
ExceptionTranslationFilter
AuthorizationFilter
```

Their responsibilities are different:

- **HeaderWriterFilter** — writes configured security-related response headers.
- **CorsFilter** — handles CORS processing.
- **RequestCacheAwareFilter** — supports request caching for authentication flows.
- **SecurityContextHolderAwareRequestFilter** — makes the request security-aware.
- **AnonymousAuthenticationFilter** — supplies anonymous authentication when no authenticated identity exists.
- **ExceptionTranslationFilter** — translates security exceptions into the appropriate authentication/authorization response flow.
- **AuthorizationFilter** — evaluates request authorization rules.

The exact chain is version/configuration dependent.

---

## 🛡️ CSRF

For a browser session application, CSRF protection matters because browsers automatically send cookies.

If the configuration contains:

```java
.csrf(csrf -> csrf.disable())
```

then CSRF protection is explicitly disabled.

Do not carry this decision blindly into production; choose it according to the application's authentication and client model.

---

## 🚪 Logout

Session logout commonly means:

```text
Logout request
 ↓
LogoutFilter
 ↓
clear Authentication
 ↓
invalidate HTTP Session
```

JWT is different. Invalidating an HTTP session does not automatically invalidate an already-issued JWT.

JWT logout/revocation is therefore a separate design problem involving choices such as short-lived access tokens, refresh tokens, revocation state, or other server-side controls.

---

## 🎯 Method Security

Request authorization:

```java
.requestMatchers("/admin/**").hasRole("ADMIN")
```

protects the HTTP request path.

Method security:

```java
@PreAuthorize("hasRole('ADMIN')")
```

protects method execution.

This is useful when the endpoint can be reached by multiple routes or when authorization depends on the specific operation/data.

The important flow remains:

```text
Authentication
 ↓
SecurityContext
 ↓
Authorization decision
 ↓
method executes or is denied
```

---

## 🧩 `CustomUserDetails` as an Adapter

If your domain model is:

```text
User
├── id
├── username
├── password
└── roles
```

but Spring Security expects:

```text
UserDetails
├── getUsername()
├── getPassword()
├── getAuthorities()
└── account-status methods
```

then:

```text
Spring Security
      │
      ▼
CustomUserDetails
      │
      ▼
User entity
```

is an adapter between your application's representation and Spring Security's expected representation.

---

## 🔥 The Deepest Mental Model

There are two independent questions:

### 1. How do we establish Authentication?

```text
Session → restore from server-side state
JWT     → validate token and create Authentication
Basic   → authenticate supplied credentials
OAuth2  → use the relevant OAuth2 authentication mechanism
```

### 2. What do we do after Authentication exists?

```text
SecurityContext
      ↓
AuthorizationFilter / method security
      ↓
ALLOW / DENY
```

This separation is why the same:

```java
.anyRequest().authenticated()
```

can work with session, JWT, Basic, or OAuth2.

---

