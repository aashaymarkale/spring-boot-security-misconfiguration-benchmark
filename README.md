# Spring Boot Security Misconfiguration Benchmark

A curated benchmark dataset of deliberately misconfigured
Spring Boot applications, designed to evaluate LLM-augmented
static analysis tools for security misconfiguration detection.

> Part of the thesis:
> **"A Systematic Evaluation of LLM-Augmented Static Analysis
> for Detecting Security Misconfigurations in Spring Boot
> Applications"**
> Aashay Ajay Markale — M.Sc. Global Software Development —
> Hochschule Fulda — 2026

> ⚠️ **WARNING:** Every application in this repository contains
> **intentional security misconfigurations** (including committed
> credentials, which are fake). They exist for research and
> education only. Do **not** deploy any of them anywhere real.

## Structure

| App | Type | Port | TPs | FPs | Ambiguous | Key scenarios |
|---|---|---|---|---|---|---|
| [`demo-rest-api`](demo-rest-api) | Stateless JWT REST API | 8080 | 6 | 3 | 0 | CSRF-FP, CORS-FP, actuator, hardcoded secrets, IDOR |
| [`demo-web-app`](demo-web-app) | Session-based web app (Thymeleaf) | 8081 | 7 | 0 | 0 | CSRF-TP, CORS-TP, exposed admin panel, insecure cookie, IDOR |
| [`demo-microservice`](demo-microservice) | API-key microservice with dev/prod profiles | 8082 | 4 | 10 | 1 | Profile-specific config, TLS at ingress, ignored `@PreAuthorize`, boundary cases |

**Total: 31 labelled findings (17 TP, 13 FP, 1 ambiguous).**

TP = genuine misconfiguration, FP = flagged by scanners but justified
by the surrounding architecture. Labels are assigned by the author
from each app's documented architecture, not from tool output. Each
app's README has its full vulnerability inventory, and every finding
is annotated in the source with a `VULNERABILITY-*` comment.

## Design: matched pairs

The apps are built so the same code pattern appears with opposite
ground-truth labels, depending on context the scanner cannot see in
the flagged line:

| Setting | FP instance | TP instance | Deciding signal |
|---|---|---|---|
| `csrf().disable()` | App 1 (stateless JWT, no cookies) | App 2 (session cookies) | Authentication architecture |
| CORS config | App 1 (specific origins, no credentials) | App 2 (wildcard origin + credentials) | Origins and credentials |
| `permitAll()` | App 1 (health/version endpoints) | App 2 (admin user list) | Sensitivity of the endpoint |
| `include-stacktrace: always`, H2 console, hardcoded keys | App 3 `dev` profile | App 3 `prod` profile | Active Spring profile |
| `server.ssl.enabled=false` | App 3 (TLS terminated at ingress) | Apps 1 and 2 | Infrastructure outside the code |

App 3 additionally covers harder boundary cases: evidence split across
files (`@PreAuthorize` without `@EnableMethodSecurity`), a finding that
is undecidable from code alone (`/internal/**` relying on network
policy), and true-negative controls that must not be flagged.

## Running the apps

Each app is an independent Maven project (Java 17, Spring Boot 3.2.5):

```bash
cd demo-rest-api     && mvn spring-boot:run   # http://localhost:8080
cd demo-web-app      && mvn spring-boot:run   # http://localhost:8081
cd demo-microservice && mvn spring-boot:run   # http://localhost:8082 (dev profile)
```

See each app's README for demo accounts, endpoints and example requests.

## Dataset

The labelled findings are in `dataset/findings.csv` (to be added).
