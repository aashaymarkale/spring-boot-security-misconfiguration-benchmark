package com.thesis.demouserapi.config;

import com.thesis.demouserapi.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for the demo-user-api application.
 *
 * <p>This class contains deliberate misconfigurations annotated
 * with VULNERABILITY comments for use in the thesis benchmark
 * dataset. Each annotation documents the finding ID, risk level,
 * expected SonarQube rule, and expected LLM classification.
 *
 * <p>IMPORTANT: This application is for research and educational
 * purposes only. Do not deploy in production.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // ─────────────────────────────────────────────────
            // VULNERABILITY-FP [CSRF-001]: CSRF protection disabled
            // Risk (apparent): CSRF attacks on state-changing requests
            // Why this is a FALSE POSITIVE in this context:
            //   This application is a STATELESS REST API secured
            //   exclusively with JWT Bearer tokens transmitted via
            //   the Authorization header — NOT via cookies.
            //   CSRF attacks exploit the browser's automatic
            //   attachment of cookies to cross-origin requests.
            //   Since this API does not use cookies for auth,
            //   there is no CSRF attack surface. Disabling CSRF
            //   protection is the recommended practice for
            //   stateless JWT-secured REST APIs.
            // Contextual signals for LLM:
            //   - SessionCreationPolicy.STATELESS configured below
            //   - JwtAuthFilter registered (see below)
            //   - No session cookies, no Thymeleaf templates
            // Expected SonarQube rule: java:S4502 (CSRF)
            // Expected LLM classification: FALSE_POSITIVE
            // ─────────────────────────────────────────────────
            .csrf(AbstractHttpConfigurer::disable)

            // ─────────────────────────────────────────────────
            // VULNERABILITY-FP [CORS-001]: Permissive CORS for
            //   specific trusted origins — see corsConfigurationSource()
            // Note: CORS is configured separately below.
            //   SonarQube may flag the allowedOrigins pattern.
            //   See corsConfigurationSource() for annotation.
            // ─────────────────────────────────────────────────
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Stateless session — no server-side session state
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Route authorisation
            .authorizeHttpRequests(auth -> auth
                // Public endpoints — registration and login
                .requestMatchers("/api/auth/**").permitAll()

                // ─────────────────────────────────────────────
                // VULNERABILITY-FP [AUTH-001]: permitAll on
                //   public health and info endpoints
                // Why this is a FALSE POSITIVE:
                //   /api/public/** is intentionally unauthenticated.
                //   These are non-sensitive informational endpoints
                //   (e.g. /api/public/health, /api/public/version).
                //   No user data or sensitive operations exposed.
                // Expected LLM classification: FALSE_POSITIVE
                // ─────────────────────────────────────────────
                .requestMatchers("/api/public/**").permitAll()

                // H2 console (dev only — would be disabled in prod)
                .requestMatchers("/h2-console/**").permitAll()

                // All other requests require authentication
                .anyRequest().authenticated()
            )

            // Disable default frame options for H2 console (dev)
            .headers(headers ->
                headers.frameOptions(frame -> frame.sameOrigin()))

            // Register JWT filter before username/password filter
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS configuration.
     *
     * <p>VULNERABILITY-FP [CORS-001]: Permissive allowed origins pattern
     * <br>Apparent risk: Overly broad CORS policy
     * <br>Why this is a FALSE POSITIVE in this context:
     *   Origins are restricted to a specific trusted frontend domain
     *   and localhost for development. allowCredentials is set to
     *   false because this API uses JWT Bearer tokens, not cookies.
     *   Without allowCredentials(true), there is no CSRF risk from
     *   CORS misconfiguration even with broader origin patterns.
     * <br>Expected LLM classification: FALSE_POSITIVE
     *
     * <p>VULNERABILITY-TP [CORS-002]: If allowCredentials were set to
     *   true alongside allowedOrigins("*"), this would be a genuine
     *   misconfiguration. Demonstrated in demo-web-app (App 2).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Specific allowed origins — not wildcard
        configuration.setAllowedOrigins(
            Arrays.asList(
                "http://localhost:3000",
                "http://localhost:4200",
                "https://trusted-frontend.example.com"
            )
        );
        configuration.setAllowedMethods(
            Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")
        );
        configuration.setAllowedHeaders(List.of("*"));

        // VULNERABILITY-FP [CORS-001] continued:
        // allowCredentials is FALSE — JWT Bearer tokens used, not cookies.
        // This means even if origins were broader, CSRF via CORS is not
        // possible. SonarQube cannot determine this without context.
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
