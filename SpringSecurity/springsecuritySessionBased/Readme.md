# 🔐 Spring Security — Session-Based Authentication Under the Hood

> **Goal:** Understand exactly what happens in a Spring Security application using **Form Login + HTTP Session**, including **who creates the filters, which filters are added, their order, how authentication is created, how it reaches the session, and how it is restored on subsequent requests.**

---

# 1. 🧠 Big Picture

 configuration:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/profile/**")
                    .hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults())
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
            );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }
}
```

does **not itself contain all the security logic**.

Instead:

```text
    Security Configuration
          ↓
      HttpSecurity
          ↓
Spring Security configures/builds
          ↓
   SecurityFilterChain
          ↓
     Security Filters
          ↓
     Incoming Request
```

---

# 2. 🏗️ Who Creates the Security Filters?

This is one of the most important concepts.

You don't manually create:

```java
new UsernamePasswordAuthenticationFilter()
new AuthorizationFilter()
new LogoutFilter()
```

Instead, you configure features through `HttpSecurity`.

For example:

```java
.formLogin(...)
```

tells Spring Security:

> Configure form-login authentication infrastructure.

Similarly:

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

builds the final `SecurityFilterChain`.

Conceptually:

```text
                 HttpSecurity
                     │
       ┌─────────────┼──────────────┐
       ↓             ↓              ↓
   formLogin     logout()     authorizeHttpRequests()
       │             │              │
       ↓             ↓              ↓
 Authentication   Logout       Authorization
    filters       filters         filters
       │             │              │
       └─────────────┼──────────────┘
                     ↓
              SecurityFilterChain
```

---

# 3. 🔥  SecurityFilterChain

The final chain contains multiple filters.

The **exact filter list can vary depending on Spring Security version and enabled features**, but with your configuration, the important filters are:

```text
SecurityFilterChain
│
├── SecurityContextHolderFilter
├── HeaderWriterFilter
├── CorsFilter
├── LogoutFilter
├── UsernamePasswordAuthenticationFilter
├── RequestCacheAwareFilter
├── SecurityContextHolderAwareRequestFilter
├── AnonymousAuthenticationFilter
├── ExceptionTranslationFilter
└── AuthorizationFilter
```

> ⚠️ `CsrfFilter` would normally be part of a web-security setup, but **your configuration explicitly disables CSRF**, so it is not active in your chain.

There can also be additional filters depending on Spring Security version and other enabled features.

### Don't memorize the list blindly.

Understand **why each filter exists** and which configuration causes it to participate.

---

# 4. 🔄 Request Processing Pipeline

For a normal authenticated request:

```text
HTTP Request
     ↓
SecurityFilterChain
     ↓
SecurityContextHolderFilter
     ↓
Load SecurityContext
     ↓
Find session using JSESSIONID
     ↓
Retrieve Authentication
     ↓
Authentication available in SecurityContext
     ↓
...
     ↓
AuthorizationFilter
     ↓
Check authorization rules
     ↓
Controller
```

For the first login request, the flow is different because authentication must actually be performed.

---

# 5. 🧩 SecurityContext

Spring Security keeps the current user's authentication inside:

```java
SecurityContext
```

which contains:

```java
Authentication
```

Conceptually:

```text
SecurityContext
      │
      └── Authentication
             │
             ├── Principal
             ├── Authorities
             └── authenticated = true
```

The current context is exposed through:

```java
SecurityContextHolder
```

So application code can access:

```java
Authentication authentication =
    SecurityContextHolder
        .getContext()
        .getAuthentication();
```

---

# 6. 🧠 `SecurityContextHolderFilter`

This filter participates in loading the `SecurityContext` for the current request.

Conceptually:

```text
HTTP Request
      ↓
SecurityContextHolderFilter
      ↓
Load SecurityContext
      ↓
SecurityContextHolder
```

The important mental model:

> **Each request gets a SecurityContext available to the security infrastructure.**

If the user has already authenticated through a session, the context can be restored from that session.

---

# 7. 🍪 Where `JSESSIONID` Comes From

After successful login, Spring Security uses the HTTP session.

The browser receives something like:

```http
Set-Cookie: JSESSIONID=ABC123
```

The browser subsequently sends:

```http
Cookie: JSESSIONID=ABC123
```

Important:

> **The browser does NOT normally store the Authentication object in the cookie.**

The cookie contains the session identifier.

Conceptually:

```text
Browser
│
└── JSESSIONID=ABC123
             │
             │ request
             ▼
        Server
             │
             ▼
       HTTP Session
             │
             ▼
      SecurityContext
             │
             ▼
       Authentication
```

---

# 8. 🔑 Login Flow

Suppose the user submits:

```http
POST /login

username=sagar
password=password123
```

Because you configured:

```java
.formLogin(Customizer.withDefaults())
```

Spring Security configures form-login authentication infrastructure.

The important filter is:

```text
UsernamePasswordAuthenticationFilter
```

Flow:

```text
POST /login
      ↓
UsernamePasswordAuthenticationFilter
      ↓
Extract username + password
      ↓
Create UsernamePasswordAuthenticationToken
      ↓
AuthenticationManager
```

---

# 9. 🧠 `UsernamePasswordAuthenticationFilter`

This filter's job is essentially:

```text
HTTP credentials
      ↓
Authentication request
```

It does **not** directly query your database.

It delegates:

```text
UsernamePasswordAuthenticationFilter
          ↓
AuthenticationManager
```

---

# 10. 🎯 AuthenticationManager

The `AuthenticationManager` is the main authentication abstraction.

Conceptually:

```java
Authentication authenticate(
        Authentication authentication)
```

Its job is:

> Find an appropriate `AuthenticationProvider` and ask it to authenticate the request.

Your chain becomes:

```text
UsernamePasswordAuthenticationFilter
             ↓
       AuthenticationManager
             ↓
      AuthenticationProvider
```

---

# 11. 🗄️ Your `DaoAuthenticationProvider`

You explicitly registered:

```java
@Bean
public DaoAuthenticationProvider authenticationProvider(
        UserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder) {

    DaoAuthenticationProvider provider =
            new DaoAuthenticationProvider(userDetailsService);

    provider.setPasswordEncoder(passwordEncoder);

    return provider;
}
```

So for username/password authentication:

```text
AuthenticationManager
        ↓
DaoAuthenticationProvider
```

`DaoAuthenticationProvider` performs the standard DAO-style authentication flow.

---

# 12. 👤 `UserDetailsService`

Your implementation:

```java
@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        return new CustomUserDetails(
            userRepository
                .findByUsername(username)
                .orElseThrow(...)
        );
    }
}
```

Flow:

```text
DaoAuthenticationProvider
        ↓
UserDetailsService
        ↓
loadUserByUsername(username)
        ↓
UserRepository
        ↓
Database
        ↓
User
        ↓
CustomUserDetails
```

---

# 13. 🔌 CustomUserDetails = Adapter

Your domain object:

```text
User
├── username
├── password
├── roles
└── ...
```

Spring Security expects:

```text
UserDetails
├── getUsername()
├── getPassword()
├── getAuthorities()
└── ...
```

So:

```text
             Spring Security
                   │
             expects UserDetails
                   ▲
                   │
          CustomUserDetails
             Adapter
                   │
                   ▼
                  User
```

`CustomUserDetails` adapts your application's `User` model to Spring Security's `UserDetails` interface.

---

# 14. 🔐 Password Verification

Your bean:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

is injected into:

```text
DaoAuthenticationProvider
```

Authentication:

```text
Raw password
     │
     ▼
PasswordEncoder.matches()
     │
     ▼
Stored BCrypt hash
```

Conceptually:

```text
password123
     ↓
BCrypt comparison
     ↓
stored hash
     ↓
MATCH?
```

If valid:

```text
Authentication SUCCESS
```

If invalid:

```text
AuthenticationException
```

---

# 15. ✅ Successful Authentication

After successful authentication:

```text
Authentication
│
├── principal
│     └── CustomUserDetails
│
├── authorities
│     ├── ROLE_USER
│     └── ...
│
└── authenticated = true
```

This authenticated object is associated with the current:

```text
SecurityContext
```

Conceptually:

```text
Authentication
      ↓
SecurityContext
      ↓
SecurityContextHolder
```

---

# 16. 💾 How Session Authentication Works

Once authentication succeeds, Spring Security's security-context/session infrastructure associates the security context with the HTTP session.

Conceptually:

```text
Authentication
      ↓
SecurityContext
      ↓
HTTP Session
      ↓
JSESSIONID
      ↓
Browser
```

The server-side state is roughly:

```text
HTTP Session ABC123
        │
        └── SecurityContext
                 │
                 └── Authentication
```

The browser only needs:

```text
JSESSIONID=ABC123
```

---

# 17. 🔄 Second Request

The user now requests:

```http
GET /profile
Cookie: JSESSIONID=ABC123
```

There is **no username/password login again**.

The request goes through the chain:

```text
HTTP Request
     ↓
SecurityFilterChain
     ↓
SecurityContextHolderFilter
     ↓
Load SecurityContext
     ↓
HTTP Session
     ↓
JSESSIONID = ABC123
     ↓
SecurityContext
     ↓
Authentication
     ↓
AuthorizationFilter
     ↓
Authorization decision
     ↓
Controller
```

This is why session-based authentication is **stateful**.

The server remembers the authentication.

---

# 18. 🛡️ AuthorizationFilter

Your configuration:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/public/**").permitAll()

    .requestMatchers("/admin/**")
        .hasRole("ADMIN")

    .requestMatchers("/profile/**")
        .hasAnyRole("USER", "ADMIN")

    .anyRequest().authenticated()
)
```

is used by the authorization infrastructure.

The important filter here is:

```text
AuthorizationFilter
```

It asks:

> **Does the current Authentication satisfy the authorization rule for this request?**

---

# 19. `permitAll()`

```java
.requestMatchers("/public/**").permitAll()
```

Means:

```text
/public/**
      ↓
No authentication required
      ↓
ALLOW
```

It does **not** mean the request skips the entire security filter chain.

---

# 20. `hasRole("ADMIN")`

```java
.requestMatchers("/admin/**")
    .hasRole("ADMIN")
```

Conceptually:

```text
GET /admin
     ↓
Authentication?
     ↓
Authorities?
     ↓
ROLE_ADMIN?
```

Remember:

```java
hasRole("ADMIN")
```

normally checks:

```text
ROLE_ADMIN
```

So:

```text
Authentication
     │
     └── Authorities
            │
            └── ROLE_ADMIN
```

---

# 21. `hasAnyRole("USER", "ADMIN")`

```java
.requestMatchers("/profile/**")
    .hasAnyRole("USER", "ADMIN")
```

means:

```text
ROLE_USER OR ROLE_ADMIN
```

must exist in the authenticated user's authorities.

---

# 22. `.authenticated()`

```java
.anyRequest().authenticated()
```

does **not create authentication**.

It only establishes an authorization requirement:

```text
Authentication exists?
       │
       ├── No → 401
       │
       └── Yes → continue
```

This is extremely important when later implementing JWT.

`.authenticated()` doesn't care whether the authentication came from:

```text
Session
Basic Authentication
JWT
OAuth2
Custom filter
```

It only cares about the resulting:

```text
Authentication
```

---

# 23. 🚨 ExceptionTranslationFilter

If authorization cannot proceed because authentication/authorization fails, Spring Security needs to translate the security exception into an HTTP response.

This is where:

```text
ExceptionTranslationFilter
```

plays an important role.

Conceptually:

```text
Security exception
      ↓
ExceptionTranslationFilter
      ↓
┌───────────────┬─────────────────┐
│               │
Unauthenticated Authenticated but
                forbidden
│               │
▼               ▼
401             403
```

---

# 24. 👤 AnonymousAuthenticationFilter

If there is no authenticated user, Spring Security can establish an anonymous authentication representation.

Conceptually:

```text
No Authentication
       ↓
AnonymousAuthenticationFilter
       ↓
AnonymousAuthenticationToken
```

This is useful because security infrastructure can consistently reason about an anonymous request instead of always dealing with `null`.

So mentally:

```text
Authenticated user
      ↓
Authentication

No authenticated user
      ↓
Anonymous Authentication
```

Don't confuse anonymous authentication with a logged-in user.

---

# 25. 🚪 LogoutFilter

Your configuration:

```java
.logout(logout -> logout
    .logoutUrl("/auth/logout")
    .invalidateHttpSession(true)
    .clearAuthentication(true)
)
```

configures logout handling.

Request:

```http
POST /auth/logout
```

Flow:

```text
Request
  ↓
LogoutFilter
  ↓
Clear Authentication
  ↓
Invalidate HTTP Session
  ↓
Authenticated session state gone
```

You specifically configured:

```java
invalidateHttpSession(true)
```

and:

```java
clearAuthentication(true)
```

---

# 26. 🌐 CorsFilter

You configured:

```java
.cors(Customizer.withDefaults())
```

This enables Spring Security's CORS integration.

Conceptually:

```text
Browser
   ↓
Cross-Origin Request
   ↓
CorsFilter
   ↓
CORS policy
```

CORS is about whether a browser is allowed to make/read cross-origin requests.

It is **not authentication**.

---

# 27. 🛡️ CSRF

You explicitly wrote:

```java
.csrf(csrf -> csrf.disable())
```

Therefore the CSRF protection filter is disabled for this configuration.

Normally, Spring Security's web setup includes CSRF protection.

With your configuration:

```text
CSRF filter
     ↓
DISABLED ❌
```

This matters because you are using **session authentication**, where browsers automatically send cookies.

For a production browser-based session application, blindly disabling CSRF is generally not something to do without understanding the threat model.

---

# 28. 🧾 HeaderWriterFilter

Spring Security can add security-related HTTP headers to responses.

Conceptually:

```text
Response
   ↓
HeaderWriterFilter
   ↓
Security-related headers
   ↓
Browser
```

The exact headers depend on the configured Spring Security version and settings.

---

# 29. 📦 Other infrastructure filters

Your chain may also contain infrastructure filters such as:

```text
RequestCacheAwareFilter
SecurityContextHolderAwareRequestFilter
```

### RequestCacheAwareFilter

Helps Spring Security remember requests in scenarios where authentication is required before the original request can continue.

Example:

```text
GET /admin
      ↓
Authentication required
      ↓
Remember original request
      ↓
Login
      ↓
Continue toward original request
```

### SecurityContextHolderAwareRequestFilter

Wraps the request to expose Spring Security-aware request methods/behavior to application code.

These are infrastructure pieces rather than your main authentication provider.

---

# 30. 🧩 Your Important Filter Chain

For your current setup, think about the chain approximately like this:

```text
┌────────────────────────────────────────────┐
│           SecurityFilterChain              │
├────────────────────────────────────────────┤
│                                            │
│ SecurityContextHolderFilter                │
│        ↓                                   │
│ HeaderWriterFilter                         │
│        ↓                                   │
│ CorsFilter                                 │
│        ↓                                   │
│ LogoutFilter                               │
│        ↓                                   │
│ UsernamePasswordAuthenticationFilter       │
│        ↓                                   │
│ RequestCacheAwareFilter                    │
│        ↓                                   │
│ SecurityContextHolderAwareRequestFilter    │
│        ↓                                   │
│ AnonymousAuthenticationFilter              │
│        ↓                                   │
│ ExceptionTranslationFilter                 │
│        ↓                                   │
│ AuthorizationFilter                        │
│                                            │
└────────────────────────────────────────────┘
```

> **Important:** This is a conceptual view of the relevant chain for your configuration. The exact generated chain and ordering can differ by Spring Security release and additional auto-configuration.

---

# 31. 🔥 What Configuration Adds/Controls What?

This is the part worth remembering from your `SecurityConfig`.

| Configuration                    | Security infrastructure it enables/configures                               |
| -------------------------------- | --------------------------------------------------------------------------- |
| `cors()`                         | CORS handling, typically `CorsFilter`                                       |
| `csrf()`                         | CSRF protection; **you disabled it**                                        |
| `authorizeHttpRequests()`        | Authorization infrastructure, including `AuthorizationFilter`               |
| `formLogin()`                    | Form-login authentication, including `UsernamePasswordAuthenticationFilter` |
| `logout()`                       | Logout infrastructure, including `LogoutFilter`                             |
| `http.build()`                   | Builds the final `SecurityFilterChain`                                      |
| `@EnableMethodSecurity`          | Method-security infrastructure for `@PreAuthorize`, etc.                    |
| `DaoAuthenticationProvider` bean | Username/password authentication provider                                   |
| `PasswordEncoder` bean           | Password hashing/verification used by provider                              |

---

# 32. 🧠 Authentication vs Authorization

Never mix these two.

### Authentication

> **Who are you?**

```text
username + password
       ↓
AuthenticationManager
       ↓
DaoAuthenticationProvider
       ↓
UserDetailsService
       ↓
PasswordEncoder
       ↓
Authentication
```

### Authorization

> **What are you allowed to do?**

```text
Authentication
      ↓
Authorities
      ↓
AuthorizationFilter
      ↓
Security rule
      ↓
ALLOW / DENY
```

---

# 33. 🧠 Session vs Authentication

Another important distinction:

```text
Authentication
    ↓
Represents who the user is
```

while:

```text
HTTP Session
    ↓
Server-side state associated with that user's session
```

Spring Security uses the session to persist the security context between requests.

```text
Browser
   │
   │ JSESSIONID
   ▼
HTTP Session
   │
   ▼
SecurityContext
   │
   ▼
Authentication
```

---

# 34. 🔄 Complete Login Flow

```text
                         LOGIN
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
                 ┌─────────┴─────────┐
                 ▼                   ▼
       CustomUserDetailsService   PasswordEncoder
                 │                   │
                 ▼                   │
          UserRepository             │
                 │                   │
                 ▼                   │
             Database                │
                 │                   │
                 ▼                   │
        CustomUserDetails            │
                 │                   │
                 └─────────┬─────────┘
                           ▼
                    Authentication
                           │
                           ▼
                    SecurityContext
                           │
                           ▼
                      HTTP Session
                           │
                           ▼
                       JSESSIONID
                           │
                           ▼
                        Browser
```

---

# 35. 🔄 Complete Subsequent Request

```text
                    NEXT REQUEST
                         │
                         ▼
                  HTTP Request
                         │
                         │ Cookie: JSESSIONID
                         ▼
              SecurityFilterChain
                         │
                         ▼
             SecurityContextHolderFilter
                         │
                         ▼
               Load SecurityContext
                         │
                         ▼
                   HTTP Session
                         │
                         ▼
                  SecurityContext
                         │
                         ▼
                  Authentication
                         │
                         ▼
              Anonymous / other
              security infrastructure
                         │
                         ▼
               AuthorizationFilter
                         │
                         ▼
                Authorization Rules
                         │
              ┌──────────┼──────────┐
              ▼          ▼          ▼
          permitAll   hasRole()  authenticated()
              │          │          │
              └──────────┼──────────┘
                         ▼
                    Controller
                         │
                         ▼
                      Service
                         │
                         ▼
                   @PreAuthorize
                         │
                         ▼
                    Method runs
```



---
# 36. 🔀 Actual Filter Chain — Same Chain, Different Request Paths

The most important mental model:

```text
                EVERY HTTP REQUEST
                       │
                       ▼
             Same SecurityFilterChain
                       │
                       ▼
       ┌─────────────────────────────────┐
       │ SecurityContextHolderFilter     │
       │ HeaderWriterFilter              │
       │ CorsFilter                      │
       │ LogoutFilter                    │
       │ UsernamePasswordAuthentication  │
       │ Filter                          │
       │ RequestCacheAwareFilter         │
       │ SecurityContextHolderAware      │
       │ RequestFilter                   │
       │ AnonymousAuthenticationFilter   │
       │ ExceptionTranslationFilter      │
       │ AuthorizationFilter             │
       └─────────────────────────────────┘
                       │
                       ▼
                  Controller
```

> **The filters don't dynamically disappear or get reordered depending on whether the request is login/non-login. The same chain is executed. Each filter decides whether it has work to do for that particular request.**

---

# 37. 🔑 LOGIN REQUEST — `POST /login`

Example:

```http
POST /login
Content-Type: application/x-www-form-urlencoded

username=sagar&password=1234
```

The same chain starts:

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
                       HeaderWriter
                             │
                             ▼
                         CorsFilter
                             │
                             ▼
                       LogoutFilter
                             │
                             │
                             │ Not /logout
                             │ → continues
                             ▼
          UsernamePasswordAuthenticationFilter
                             │
                             │ Matches /login
                             ▼
                   Extract credentials
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
                    ┌────────┴────────┐
                    ▼                 ▼
          UserDetailsService     PasswordEncoder
                    │                 │
                    ▼                 │
             UserRepository           │
                    │                 │
                    ▼                 │
                Database              │
                    │                 │
                    ▼                 │
           CustomUserDetails           │
                    │                 │
                    └────────┬────────┘
                             ▼
                      Authentication
                             │
                             ▼
                      SecurityContext
                             │
                             ▼
                 SecurityContext/session
                    persistence
                             │
                             ▼
                      HTTP Session
                             │
                             ▼
                       JSESSIONID
                             │
                             ▼
                          Browser
```

### What about the filters after `UsernamePasswordAuthenticationFilter`?

This is important:

**Successful authentication changes what happens next.**

The authentication filter handles the login attempt and, on success, invokes its success handling. The request is normally completed/redirected rather than simply continuing through the rest of the chain as if it were an ordinary protected request.

So don't mentally model login as:

```text
UsernamePasswordAuthenticationFilter
        ↓
AuthorizationFilter
        ↓
Controller
```

The login filter **handles the authentication request itself**.

---

# 38. 🔄 NORMAL REQUEST WITH `JSESSIONID`

Example:

```http
GET /profile
Cookie: JSESSIONID=ABC123
```

Same chain:

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
                  Load SecurityContext
                            │
                            ▼
                 HTTP Session ABC123
                            │
                            ▼
                    SecurityContext
                            │
                            ▼
                    Authentication
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
                            │ Not /logout
                            ▼
          UsernamePasswordAuthenticationFilter
                            │
                            │ Not /login
                            ▼
                 RequestCacheAwareFilter
                            │
                            ▼
          SecurityContextHolderAwareRequestFilter
                            │
                            ▼
              AnonymousAuthenticationFilter
                            │
                            │ Authentication already exists
                            │ → does nothing
                            ▼
              ExceptionTranslationFilter
                            │
                            ▼
                  AuthorizationFilter
                            │
                            ▼
                  /profile/** rule
                            │
                            ▼
               hasAnyRole(USER, ADMIN)
                            │
                            ▼
                       Controller
                            │
                            ▼
                         Service
```

So:

```text
JSESSIONID exists
       ↓
SecurityContext restored
       ↓
Authentication exists
       ↓
AnonymousAuthenticationFilter doesn't replace it
       ↓
AuthorizationFilter checks it
```

---

# 39. 🚫 NORMAL REQUEST WITHOUT `JSESSIONID`

Example:

```http
GET /profile
```

No cookie.

Same exact chain:

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
                 No existing session
                            │
                            ▼
              No authenticated SecurityContext
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
                            ▼
          UsernamePasswordAuthenticationFilter
                            │
                            │ Not /login
                            ▼
                 RequestCacheAwareFilter
                            │
                            ▼
          SecurityContextHolderAwareRequestFilter
                            │
                            ▼
              AnonymousAuthenticationFilter
                            │
                            ▼
                  AnonymousAuthentication
                            │
                            ▼
              ExceptionTranslationFilter
                            │
                            ▼
                  AuthorizationFilter
                            │
                            ▼
               /profile/** requires
                  USER or ADMIN
                            │
                            ▼
                     ❌ DENIED
                            │
                            ▼
                           401
```

---

# 40. ⚠️ LOGIN REQUEST WITH AN EXISTING `JSESSIONID`

This is the subtle case you mentioned.

Suppose the browser already has:

```text
JSESSIONID=ABC123
```

and then sends:

```http
POST /login
Cookie: JSESSIONID=ABC123
```

The request **still goes through the exact same filter chain**.

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
              Existing session?
                         │
                         ▼
              Load existing context
                         │
                         ▼
                 Authentication?
                         │
                         ▼
             Continue through filters
                         │
                         ▼
                  LogoutFilter
                         │
                         ▼
      UsernamePasswordAuthenticationFilter
                         │
                         │ Matches /login
                         ▼
              Authenticate credentials
                         │
                         ▼
                  Authentication
                         │
                         ▼
             Successful authentication
                         │
                         ▼
              Save security context
                         │
                         ▼
                    Session
```

So:

> **`JSESSIONID` does not decide whether the request is a login request.**

The request itself determines that:

```text
POST /login
     ↓
UsernamePasswordAuthenticationFilter
     ↓
login processing
```

Whereas:

```text
GET /profile
     ↓
UsernamePasswordAuthenticationFilter
     ↓
not a login request
     ↓
continues
```

---

# 41. 🧠 The Three Main Branches

You can put this compact diagram directly after your two previous flows:

```text
                         HTTP REQUEST
                              │
                              ▼
                    SecurityFilterChain
                              │
                              ▼
               SecurityContextHolderFilter
                              │
                 ┌────────────┴────────────┐
                 │                         │
                 ▼                         ▼
          JSESSIONID exists          No JSESSIONID
                 │                         │
                 ▼                         ▼
        Load SecurityContext       No existing context
                 │                         │
                 ▼                         ▼
          Authentication?         Anonymous later
                 │                         │
                 └────────────┬────────────┘
                              ▼
                       Remaining Filters
                              │
                              ▼
                       LogoutFilter
                              │
                              ▼
              UsernamePasswordAuthenticationFilter
                              │
                    ┌─────────┴─────────┐
                    │                   │
              /login request       Not /login
                    │                   │
                    ▼                   ▼
              Authenticate          Continue
                    │                   │
                    └─────────┬─────────┘
                              ▼
               SecurityContextHolderAware
                     RequestFilter
                              │
                              ▼
                AnonymousAuthenticationFilter
                              │
                    ┌─────────┴─────────┐
                    │                   │
          Authentication exists    No Authentication
                    │                   │
                do nothing              ▼
                                    Anonymous
                              │
                              ▼
                 ExceptionTranslationFilter
                              │
                              ▼
                    AuthorizationFilter
                              │
                              ▼
                  Authorization Decision
                              │
                              ▼
                         Controller
```

---

# 42. 🔥 The Most Important Insight

Don't think:

```text
Login request → Login filters
Normal request → Different filters
```

Think:

```text
                    SAME FILTER CHAIN
                           │
              ┌────────────┼────────────┐
              │            │            │
           Login        With Session   No Session
              │            │            │
              ▼            ▼            ▼
        Filters decide what to do for THIS request
```

The filters are **already configured when `http.build()` creates the chain**.

At runtime, every request enters that chain.

Each filter effectively asks:

```text
"Is this request relevant to my responsibility?"
```

For example:

```text
LogoutFilter
     ↓
Is this /logout?
     ├── YES → logout
     └── NO  → continue

UsernamePasswordAuthenticationFilter
     ↓
Is this /login?
     ├── YES → authenticate
     └── NO  → continue

AuthorizationFilter
     ↓
What authorization rule matches this request?
     ↓
Does current Authentication satisfy it?
     ├── YES → continue
     └── NO  → deny
```

That's the **actual runtime mental model** of your `SecurityFilterChain`.

# 43. 🚫 Authenticated but wrong role

Suppose:

```text
Authentication
    ↓
ROLE_USER
```

and:

```text
/admin/**
    ↓
ROLE_ADMIN required
```

Then:

```text
Request
   ↓
Authentication = USER
   ↓
AuthorizationFilter
   ↓
ROLE_ADMIN?
   ↓
NO
   ↓
403 Forbidden
```

Therefore:

```text
401 → "You aren't authenticated."

403 → "You are authenticated, but you aren't allowed."
```

---

# 44. 🧩 Why JWT will look different

Your current application:

```text
Session-based
```

uses:

```text
Login
 ↓
Authentication
 ↓
SecurityContext
 ↓
Session
 ↓
JSESSIONID
```

Later, if you implement your own JWT authentication:

```text
Request
 ↓
JWT Filter
 ↓
Extract Bearer token
 ↓
Validate JWT
 ↓
Create Authentication
 ↓
SecurityContext
 ↓
AuthorizationFilter
```

The crucial difference:

### Session

```text
Authentication
      ↓
stored/reused through server-side session
```

### JWT

```text
JWT
 ↓
re-authenticate request
 ↓
Authentication
 ↓
SecurityContext
```

And `.authenticated()` remains essentially the same.

It only cares about:

```text
SecurityContext
      ↓
Authentication
      ↓
authenticated?
```
