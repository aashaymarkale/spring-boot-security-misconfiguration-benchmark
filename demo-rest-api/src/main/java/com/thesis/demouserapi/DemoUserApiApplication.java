package com.thesis.demouserapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demo User Management REST API
 *
 * <p>Deliberately misconfigured Spring Boot application for use
 * as part of the benchmark dataset in the thesis:
 * "A Systematic Evaluation of LLM-Augmented Static Analysis for
 * Detecting Security Misconfigurations in Spring Boot Applications"
 *
 * <p>Aashay Ajay Markale — Hochschule Fulda — 2026
 *
 * <p><b>WARNING:</b> This application contains intentional security
 * misconfigurations. Do NOT deploy in any production or publicly
 * accessible environment.
 */
@SpringBootApplication
public class DemoUserApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoUserApiApplication.class, args);
    }
}
