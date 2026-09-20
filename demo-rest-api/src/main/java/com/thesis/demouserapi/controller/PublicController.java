package com.thesis.demouserapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Public informational endpoints — intentionally unauthenticated.
 *
 * <p>VULNERABILITY-FP [AUTH-001] (see SecurityConfig):
 * The permitAll() on /api/public/** is intentional and safe.
 * These endpoints expose no sensitive data — only application
 * name and version, which are appropriate for health checks
 * and service discovery in a microservice environment.
 */
@RestController
@RequestMapping("/api/public")
public class PublicController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    @GetMapping("/version")
    public ResponseEntity<Map<String, String>> version() {
        return ResponseEntity.ok(Map.of(
            "application", "demo-user-api",
            "version", "1.0.0"
        ));
    }
}
