# demo-user-api

A deliberately misconfigured Spring Boot user management REST API.
Part of the benchmark dataset for the thesis:

> **"A Systematic Evaluation of LLM-Augmented Static Analysis for
> Detecting Security Misconfigurations in Spring Boot Applications"**
> Aashay Ajay Markale — M.Sc. Global Software Development —
> Hochschule Fulda — 2026

---

> ⚠️ **WARNING:** This application contains **intentional security
> misconfigurations**. It is designed exclusively for research and
> educational purposes. Do **not** deploy in any production or
> publicly accessible environment.

---

## Purpose

This application is App 1 of the benchmark dataset. It is a
**stateless JWT-secured REST API** — a context in which some
configurations that appear dangerous to static analysis tools
are actually architecturally justified.

The key research question this app helps answer is:

> Can an LLM correctly distinguish true positives (genuine
> misconfigurations) from false positives (justified configurations)
> when given contextual signals about the application's architecture?

---

## Vulnerability Inventory

Every misconfiguration is annotated with `// VULNERABILITY-TP` or
`// VULNERABILITY-FP` comments in the source code. Search for
`VULNERABILITY` across the codebase to find them all.

| ID | Category | File | Type | Description |
|---|---|---|---|---|
| CRED-001 | Hardcoded credentials | `application.properties` | **TP** | Database password hardcoded |
| CRED-002 | Hardcoded credentials | `application.properties` | **TP** | JWT signing secret hardcoded |
| ACT-001 | Actuator exposure | `application.properties` | **TP** | All actuator endpoints exposed unauthenticated |
| SSL-001 | Missing HTTPS | `application.properties` | **TP** | SSL disabled, no HTTPS enforcement |
| CSRF-001 | CSRF disabled | `SecurityConfig.java` | **FP** | CSRF disabled — justified (stateless JWT, no cookies) |
| CORS-001 | CORS config | `SecurityConfig.java` | **FP** | CORS configured — justified (specific origins, no credentials) |
| AUTH-001 | permitAll | `SecurityConfig.java` | **FP** | permitAll on /api/public/** — justified (non-sensitive endpoints) |
| AUTHZ-001 | Missing authz check | `UserService.java` | **TP** | IDOR — any user can read any profile by ID |
| AUTHZ-002 | Missing authz check | `UserService.java` | **TP** | Any user can delete any account |

**Summary: 6 True Positives, 3 False Positives**

---

## Why the FPs Are FPs

### CSRF-001 — CSRF disabled
Static scanners flag `csrf().disable()` as a critical vulnerability.
In this application it is **not dangerous** because:
- `SessionCreationPolicy.STATELESS` is configured
- `JwtAuthFilter` validates Bearer tokens from `Authorization` header
- No session cookies are used — the CSRF attack vector does not exist

### CORS-001 — CORS configuration
Static scanners may flag any CORS configuration. Here:
- Allowed origins are **specific** (not wildcard)
- `allowCredentials` is **false** (JWT Bearer, not cookies)
- No CSRF risk exists from this CORS configuration

### AUTH-001 — permitAll on public endpoints
Static scanners flag `permitAll()`. Here:
- `/api/public/health` and `/api/public/version` are intentionally public
- No sensitive data is exposed by these endpoints
- This is correct DevOps practice for health checks

---

## Running the Application

```bash
# Start the application
./mvnw spring-boot:run

# Run SonarQube scan (requires local SonarQube on port 9000)
./mvnw sonar:sonar \
  -Dsonar.projectKey=demo-user-api \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=<your-token>
```

---

## API Endpoints

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | None | Register new user |
| POST | `/api/auth/login` | None | Login, returns JWT |
| GET | `/api/users` | ADMIN | List all users |
| GET | `/api/users/me` | Any auth | Get own profile |
| GET | `/api/users/{id}` | Any auth | Get user by ID (**IDOR**) |
| DELETE | `/api/users/{id}` | Any auth | Delete user (**missing authz**) |
| GET | `/api/public/health` | None | Health check |
| GET | `/api/public/version` | None | Version info |
| GET | `/actuator/**` | None | All actuator endpoints (**exposed**) |

---

## Tech Stack

- Java 17
- Spring Boot 3.2.5
- Spring Security 6
- Spring Data JPA
- H2 (in-memory database)
- JJWT 0.11.5
- Lombok
