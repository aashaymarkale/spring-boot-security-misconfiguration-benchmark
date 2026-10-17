package com.thesis.demomicroservice.config;

import com.thesis.demomicroservice.security.ApiKeyAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security configuration for demo-microservice (App 3).
 *
 * <p>NOTE: this class deliberately does NOT declare
 * {@code @EnableMethodSecurity}. See AUTHZ-004 in ItemService.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.security.api-key}")
    private String apiKey;

    @Value("${app.security.admin-api-key}")
    private String adminApiKey;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            // ─────────────────────────────────────────────────
            // VULNERABILITY-FP [CSRF-003]: CSRF protection disabled
            // Why this is a FALSE POSITIVE:
            //   Stateless service-to-service API. Authentication is
            //   the X-API-KEY request header (see ApiKeyAuthFilter),
            //   sessions are STATELESS, and there are no cookies and
            //   no server-rendered forms. Without ambient cookie
            //   credentials there is no CSRF attack surface.
            // Boundary case: NO JWT filter exists here. The safe
            //   context must be inferred from the header-based
            //   OncePerRequestFilter + STATELESS policy instead.
            // Expected LLM classification: FALSE_POSITIVE
            // ─────────────────────────────────────────────────
            .csrf(AbstractHttpConfigurer::disable)

            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .sessionManagement(s ->
                s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()

                // Other actuator endpoints require an authenticated
                // service caller (relevant to ACT-003 in dev profile).
                .requestMatchers("/actuator/**").hasRole("SERVICE")

                // ─────────────────────────────────────────────
                // VULNERABILITY-AMBIGUOUS [AUTH-004]: permitAll on
                //   /internal/** relies on network-level isolation
                // The endpoint exposes inventory statistics without
                // any application-level authentication. The intent
                // is "in-cluster callers only", enforced (supposedly)
                // by a Kubernetes NetworkPolicy -- but no such policy
                // exists in this repository, and k8s/ingress.yaml
                // routes only /api/** externally.
                // Why AMBIGUOUS: whether this is dangerous depends
                // on infrastructure that is not visible in the code.
                // A careful reviewer would neither dismiss nor
                // confirm it from the repository alone.
                // Expected LLM classification: NEEDS_REVIEW
                // ─────────────────────────────────────────────
                .requestMatchers("/internal/**").permitAll()

                // H2 console path is permitted in every profile
                // (see H2-001 prod TP / H2-002 dev FP in the yml files).
                .requestMatchers("/h2-console/**").permitAll()

                .anyRequest().authenticated()
            )

            .headers(h -> h.frameOptions(f -> f.sameOrigin()))

            .addFilterBefore(new ApiKeyAuthFilter(apiKey, adminApiKey),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * VULNERABILITY-FP [CORS-003]: credentials allowed with an origin
     * allow-list.
     * <br>Why this is a FALSE POSITIVE: allowCredentials(true) is
     *   dangerous only in combination with a wildcard or reflected
     *   origin (compare CORS-002 in demo-web-app). Here the single
     *   allowed origin is an explicit, trusted first-party UI.
     * <br>Expected LLM classification: FALSE_POSITIVE
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(
            List.of("https://inventory-ui.example.com"));
        config.setAllowedMethods(List.of("GET", "POST", "DELETE"));
        config.setAllowedHeaders(List.of("X-API-KEY", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
