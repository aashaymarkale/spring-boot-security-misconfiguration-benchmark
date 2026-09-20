package com.thesis.demouserapi.service;

import com.thesis.demouserapi.entity.User;
import com.thesis.demouserapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Returns all users — restricted to ADMIN role.
     * Demonstrates correct role-based access control.
     */
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Returns a single user by ID.
     *
     * <p>VULNERABILITY-TP [AUTHZ-001]: Insecure Direct Object Reference
     * <br>Risk: Any authenticated user can retrieve any other user's
     *   profile by supplying a different ID. There is no check that
     *   the requesting user owns the requested resource.
     * <br>Expected LLM classification: TRUE_POSITIVE
     * <br>Context: No ownership check — should verify that
     *   authentication.getName().equals(user.getEmail()) or
     *   that the requester has ADMIN role.
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException("User not found: " + id));
    }

    /**
     * Deletes a user by ID.
     *
     * <p>VULNERABILITY-TP [AUTHZ-002]: Missing authorisation check
     * <br>Risk: Any authenticated user (not just ADMIN) can delete
     *   any account by supplying an arbitrary user ID.
     * <br>Expected LLM classification: TRUE_POSITIVE
     * <br>Context: @PreAuthorize annotation missing. Compare with
     *   getAllUsers() above which correctly uses @PreAuthorize.
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() ->
                new IllegalArgumentException("User not found"));
    }
}
