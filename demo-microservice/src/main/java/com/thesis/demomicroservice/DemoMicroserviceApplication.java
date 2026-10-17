package com.thesis.demomicroservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demo Inventory Microservice (App 3 of the benchmark).
 *
 * <p>Boundary-case application: profile-specific configuration,
 * TLS termination at an ingress, API-key (non-JWT) stateless
 * authentication, and one finding that cannot be classified from
 * code alone. Used in the thesis "A Systematic Evaluation of
 * LLM-Augmented Static Analysis for Detecting Security
 * Misconfigurations in Spring Boot Applications"
 * (Aashay Ajay Markale, Hochschule Fulda, 2026).
 *
 * <p><b>WARNING:</b> contains intentional misconfigurations.
 * Do NOT deploy in any production or publicly accessible environment.
 */
@SpringBootApplication
public class DemoMicroserviceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoMicroserviceApplication.class, args);
    }
}
