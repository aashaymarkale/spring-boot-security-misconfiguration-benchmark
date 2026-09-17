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

## Structure

| App | Type | TPs | FPs | Key scenarios |
|---|---|---|---|---|
| `demo-rest-api` | Stateless JWT REST API | 6 | 3 | CSRF-FP, actuator, hardcoded secrets |
| `demo-web-app` | Session-based web app | X | X | CSRF-TP, CORS-TP |
| `demo-microservice` | Mixed config service | X | X | Profile-specific, boundary cases |

## Dataset

The labelled findings are in `dataset/findings.csv`.
