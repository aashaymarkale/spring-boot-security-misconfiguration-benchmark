package com.thesis.demowebapp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security configuration for demo-web-app.
 *
 * <p>This application is a SESSION-BASED, COOKIE-AUTHENTICATED
 * web application using Thymeleaf server-rendered templates —
 * the architectural opposite of demo-rest-api (App 1), which is
 * stateless and JWT-secured.
 *
 * <p>This contrast is deliberate: several configurations that were
 * FALSE POSITIVES in App 1 become TRUE POSITIVES here, because the
 * underlying architecture changes the risk. This tests whether the
 * LLM reasoning layer correctly conditions its classification on
 * contextual signals rather than pattern-matching the code alone.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            // ─────────────────────────────────────────────────
            // VULNERABILITY-TP [CSRF-002]: CSRF protection disabled
            // Risk: This is a GENUINE vulnerability in this context.
            //   Unlike demo-rest-api (App 1, finding CSRF-001), this
            //   application authenticates users via session cookies
            //   (JSESSIONID), not JWT Bearer tokens. Browsers
            //   automatically attach cookies to cross-origin requests,
            //   which is exactly the attack vector CSRF protection
            //   defends against. Disabling it here allows a malicious
            //   site to submit forged requests (e.g. deleting notes,
            //   changing account settings) on behalf of a logged-in
            //   user without their knowledge.
            // Contextual signals for LLM:
            //   - No JWT filter present in this security chain
            //   - Session-based authentication (default Spring Security)
            //   - Thymeleaf templates present (server-rendered forms)
            //   - Forms submit via standard POST with cookies attached
            // Expected SonarQube rule: java:S4502 (CSRF)
            // Expected LLM classification: TRUE_POSITIVE
            //
            // NOTE: This is the SAME CODE PATTERN as CSRF-001 in
            // demo-rest-api, but with the OPPOSITE ground truth label.
            // This pair is the central test case for RQ2 and RQ3.
            // ─────────────────────────────────────────────────
            .csrf(AbstractHttpConfigurer::disable)

            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register", "/css/**", "/js/**")
                    .permitAll()
                .requestMatchers("/h2-console/**").permitAll()

                // ─────────────────────────────────────────────
                // VULNERABILITY-TP [AUTH-002]: Admin panel exposed
                //   with permitAll
                // Risk: The /admin/** path is intended to be
                //   restricted to ADMIN role users only, but
                //   permitAll() grants unauthenticated access.
                //   Unlike the FP case AUTH-001 in App 1 (which
                //   covers genuinely public health/version
                //   endpoints), /admin/users exposes a full user
                //   listing including display names and roles.
                // Expected LLM classification: TRUE_POSITIVE
                // Context: Compare with the correctly-secured
                //   /notes/** path below, which requires
                //   authentication.
                // ─────────────────────────────────────────────
                .requestMatchers("/admin/**").permitAll()

                .requestMatchers("/notes/**").authenticated()
                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/notes", true)
                .permitAll()
            )

            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )

            .headers(headers ->
                headers.frameOptions(frame -> frame.sameOrigin()))

            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    /**
     * CORS configuration.
     *
     * <p>VULNERABILITY-TP [CORS-002]: Wildcard origin with credentials
     * <br>Risk: This is a GENUINE and SEVERE misconfiguration.
     *   Allowing any origin ("*") while also allowing credentials
     *   means any website on the internet can make authenticated,
     *   cookie-bearing requests to this application on behalf of
     *   a logged-in user, and read the response. This combination
     *   is explicitly called out by browser specifications as
     *   invalid/dangerous and by security literature as a critical
     *   misconfiguration (Kakitaeva and Gaso, 2025).
     * <br>Expected LLM classification: TRUE_POSITIVE
     * <br>Context: Compare with CORS-001 in App 1, where specific
     *   origins and allowCredentials(false) made the same-shaped
     *   configuration safe. Here, both risk factors are present
     *   simultaneously: wildcard origin AND credentials allowed.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(
            List.of("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

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
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
