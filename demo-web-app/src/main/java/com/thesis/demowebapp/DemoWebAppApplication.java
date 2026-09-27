package com.thesis.demowebapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demo Session-Based Web Application
 *
 * <p>Deliberately misconfigured Spring Boot application for use
 * as part of the benchmark dataset in the thesis:
 * "A Systematic Evaluation of LLM-Augmented Static Analysis for
 * Detecting Security Misconfigurations in Spring Boot Applications"
 *
 * <p>Aashay Ajay Markale — Hochschule Fulda — 2026
 *
 * <p>This is App 2 of the benchmark: a session-based, cookie-
 * authenticated, Thymeleaf-rendered web application. It forms the
 * architectural counterpart to App 1 (demo-rest-api), which is
 * stateless and JWT-secured. Several configurations that were
 * false positives in App 1 are genuine true positives here.
 *
 * <p><b>WARNING:</b> This application contains intentional security
 * misconfigurations. Do NOT deploy in any production or publicly
 * accessible environment.
 */
@SpringBootApplication
public class DemoWebAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoWebAppApplication.class, args);
    }
}
