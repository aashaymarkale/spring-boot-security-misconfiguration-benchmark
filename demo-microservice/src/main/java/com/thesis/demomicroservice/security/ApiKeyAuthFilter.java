package com.thesis.demomicroservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * Stateless service-to-service authentication via the X-API-KEY header.
 *
 * <p>Two kinds of key exist:
 * <ul>
 *   <li>service key: grants ROLE_SERVICE (normal callers)</li>
 *   <li>admin key: grants ROLE_ADMIN and ROLE_SERVICE (privileged callers)</li>
 * </ul>
 *
 * <p>Contextual signal for the thesis pipeline: this is a
 * OncePerRequestFilter that authenticates from a request HEADER, not a
 * cookie, but it is NOT a JWT filter. A context extractor that only
 * looks for "JWT filter present" would miss that this application is
 * stateless and cookie-free. This is the reason CSRF-003 is a boundary
 * case for the ablation study (RQ3).
 */
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-API-KEY";

    private final byte[] serviceKey;
    private final byte[] adminKey;

    public ApiKeyAuthFilter(String serviceKey, String adminKey) {
        this.serviceKey = serviceKey.getBytes(StandardCharsets.UTF_8);
        this.adminKey = adminKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        String provided = request.getHeader(HEADER);

        if (provided != null) {
            byte[] candidate = provided.getBytes(StandardCharsets.UTF_8);
            if (MessageDigest.isEqual(adminKey, candidate)) {
                authenticate("admin-client", "ROLE_ADMIN", "ROLE_SERVICE");
            } else if (MessageDigest.isEqual(serviceKey, candidate)) {
                authenticate("service-client", "ROLE_SERVICE");
            }
        }

        chain.doFilter(request, response);
    }

    private void authenticate(String principal, String... roles) {
        var authorities = java.util.Arrays.stream(roles)
            .map(SimpleGrantedAuthority::new)
            .toList();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                principal, null, List.copyOf(authorities)));
    }
}
