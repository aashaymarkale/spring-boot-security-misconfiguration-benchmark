# demo-microservice

Inventory microservice with profile-specific configuration. **App 3**
of the benchmark dataset for the thesis:

> **"A Systematic Evaluation of LLM-Augmented Static Analysis for
> Detecting Security Misconfigurations in Spring Boot Applications"**
> Aashay Ajay Markale, M.Sc. Global Software Development,
> Hochschule Fulda, 2026

> ⚠️ **WARNING:** contains **intentional security misconfigurations**
> for research and education only. Do not deploy anywhere real.

## Purpose

Apps 1 and 2 test the same code pattern in two opposite architectures
(stateless JWT API vs. session-based web app). App 3 tests the harder
**boundary cases** where the deciding evidence is *not* in the flagged
line, or is spread across files:

| Boundary | Example in this app |
|---|---|
| Active profile decides risk | Same setting is FP in `application-dev.yml`, TP in `application-prod.yml` |
| Infrastructure outside the code | TLS terminated at ingress (`k8s/ingress.yaml`) |
| Stateless, but not JWT | API-key header filter (service key and admin key), so "JWT filter present" is false yet CSRF-off is safe |
| Evidence split across files | `@PreAuthorize` in a service, missing `@EnableMethodSecurity` in config |
| Genuinely undecidable from code | `/internal/**` `permitAll` relying on network policy |
| Correct controls | Settings that must **not** be flagged (true negatives) |

## Vulnerability Inventory

| ID | Category | File | Label | Description |
|---|---|---|---|---|
| CSRF-003 | CSRF disabled | `SecurityConfig.java` | **FP** | Stateless API-key auth, no cookies, no JWT |
| CORS-003 | CORS | `SecurityConfig.java` | **FP** | `allowCredentials(true)` but explicit trusted origin |
| AUTH-004 | permitAll | `SecurityConfig.java` | **AMBIGUOUS** | `/internal/**` relies on unverifiable network isolation |
| AUTHZ-004 | Missing method security | `ItemService.java` | **TP** | `@PreAuthorize("hasRole('ADMIN')")` ignored: no `@EnableMethodSecurity`, so any service-key holder can delete |
| ACT-003 | Actuator exposure | `application-dev.yml` | **FP** | `include: "*"` in dev only; prod restricts |
| CRED-003 | Hardcoded credential | `application-dev.yml` | **FP** | Throw-away in-memory H2 password |
| CRED-007 | Hardcoded credential | `application-dev.yml` | **FP** | Local-only dev API key |
| CRED-008 | Hardcoded credential | `application-dev.yml` | **FP** | Local-only dev admin API key |
| H2-002 | H2 console | `application-dev.yml` | **FP** | Dev-only convenience |
| ERR-002 | Stack traces | `application-dev.yml` | **FP** | Dev-only debugging aid |
| H2-001 | H2 console | `application-prod.yml` | **TP** | Console enabled in production, unauthenticated |
| ERR-001 | Stack traces | `application-prod.yml` | **TP** | Same setting as ERR-002, but in prod |
| CRED-004 | Hardcoded credential | `application-prod.yml` | **TP** | Production API key committed to the repo |
| SSL-003 | HTTPS not enforced | `application-prod.yml` | **FP** | TLS terminated at the ingress |
| CRED-006 | Hardcoded credential | `src/test/resources/...` | **FP** | Test fixture password |

**Summary: 4 TP, 10 FP, 1 AMBIGUOUS** (15 labelled findings)

### Controls (true negatives, no finding expected)
- `application-prod.yml`: `management.endpoints.web.exposure.include: health,info`
- `application-prod.yml`: `spring.datasource.password: ${DB_PASSWORD}` (environment-injected)
- `application-prod.yml`: `app.security.admin-api-key: ${ADMIN_API_KEY}` (environment-injected)

## Matched pairs (same setting, opposite label)

| Setting | FP instance | TP instance | Deciding signal |
|---|---|---|---|
| `include-stacktrace: always` | ERR-002 (dev) | ERR-001 (prod) | Active profile |
| H2 console enabled | H2-002 (dev) | H2-001 (prod) | Active profile / database in use |
| Hardcoded key/password | CRED-003, CRED-007, CRED-008 (dev) | CRED-004 (prod) | Active profile |
| `server.ssl.enabled=false` | SSL-003 (ingress TLS) | SSL-001/002 (Apps 1, 2) | Ingress TLS termination |

## Notes on labels

- Labels are assigned by the author from the documented architecture,
  not from tool output. The "expected SonarQube detection" statements in
  comments are hypotheses; the real scan outcome must be recorded
  separately in `findings.csv`.
- A finding the scanner never raises (for example AUTHZ-004) counts as a
  false negative for the scanner baseline (RQ1/RQ2 recall), even though
  it can never reach the LLM stage in the hybrid pipeline.

## Running

```bash
./mvnw spring-boot:run                              # dev profile, port 8082
SPRING_PROFILES_ACTIVE=prod DB_PASSWORD=x ./mvnw spring-boot:run   # needs PostgreSQL

curl -H "X-API-KEY: dev-local-key-123" localhost:8082/api/v1/items          # service key
curl -X DELETE -H "X-API-KEY: dev-local-key-123" localhost:8082/api/v1/items/1   # succeeds (AUTHZ-004: should be admin-only)
```

## Tech Stack
Java 17, Spring Boot 3.2.5, Spring Security 6, Spring Data JPA,
H2 (dev), PostgreSQL driver (prod), Lombok
