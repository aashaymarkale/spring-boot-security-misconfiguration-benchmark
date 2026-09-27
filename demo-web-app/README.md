# demo-web-app

A deliberately misconfigured Spring Boot **session-based web
application**. App 2 of the benchmark dataset for the thesis:

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

This application is the architectural counterpart to App 1
(`demo-rest-api`). Where App 1 is a **stateless, JWT-secured REST
API**, this app is a **stateful, cookie-authenticated, server-
rendered web application** using Thymeleaf templates and form-based
login.

The purpose of this contrast is to test whether the LLM reasoning
layer correctly **conditions its classification on architectural
context** rather than on the code pattern alone. Several
configurations that were false positives in App 1 become genuine
true positives here, because the same code pattern carries
different risk depending on the surrounding authentication
architecture.

---

## Vulnerability Inventory

| ID | Category | File | Type | Description |
|---|---|---|---|---|
| CSRF-002 | CSRF disabled | `SecurityConfig.java` | **TP** | CSRF disabled in a session/cookie-based app — genuinely dangerous |
| CORS-002 | CORS misconfig | `SecurityConfig.java` | **TP** | Wildcard origin + credentials allowed — critical misconfiguration |
| AUTH-002 | permitAll | `SecurityConfig.java` | **TP** | Admin user list exposed without authentication |
| ACT-002 | Actuator exposure | `application.properties` | **TP** | All actuator endpoints exposed (same category as App 1) |
| SSL-002 | Missing HTTPS | `application.properties` | **TP** | HTTPS not enforced — session cookie can be intercepted |
| SESSION-001 | Insecure cookie | `application.properties` | **TP** | Session cookie missing HttpOnly and Secure flags |
| AUTHZ-003 | Missing authz check | `NoteService.java` | **TP** | IDOR — any user can view any other user's notes |

**Summary: 7 True Positives, 0 False Positives**

This app is deliberately weighted toward true positives — it
demonstrates that the *same* misconfiguration categories which were
justified (false positive) in App 1's stateless JWT context become
genuinely dangerous (true positive) in this stateful, cookie-based
context.

---

## The Critical Contrast with App 1

| Finding | App 1 (`demo-rest-api`) | App 2 (`demo-web-app`) |
|---|---|---|
| `csrf().disable()` | **FP** — stateless JWT, no cookies | **TP** — session cookies used |
| CORS configuration | **FP** — specific origins, no credentials | **TP** — wildcard origin + credentials |
| `permitAll()` usage | **FP** — non-sensitive health endpoints | **TP** — exposes admin user list |

This pairing is the central empirical test for **RQ2** and **RQ3**:
can the LLM reasoning layer use contextual signals (session policy,
presence of a JWT filter, template usage) to correctly assign
*opposite* labels to structurally similar code?

---

## Running the Application

```bash
./mvnw spring-boot:run

# App runs on http://localhost:8081
# Demo accounts: alice / bob (password123), admin (adminpass123)
```

```bash
# SonarQube scan
./mvnw sonar:sonar \
  -Dsonar.projectKey=demo-web-app \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=<your-token>
```

---

## Routes

| Method | Route | Auth | Description |
|---|---|---|---|
| GET | `/login` | None | Login form |
| POST | `/login` | None | Authenticate |
| GET | `/notes` | Authenticated | List own notes |
| GET | `/notes/{id}` | Authenticated | View a note (**IDOR**) |
| GET | `/admin/users` | **None (misconfigured)** | List all users |
| GET | `/actuator/**` | None | All actuator endpoints (**exposed**) |

---

## Tech Stack

- Java 17
- Spring Boot 3.2.5
- Spring Security 6 (form login, session-based)
- Spring Data JPA
- Thymeleaf
- H2 (in-memory database)
- Lombok
