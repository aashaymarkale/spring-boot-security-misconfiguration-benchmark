package com.thesis.demouserapi.controller;

import com.thesis.demouserapi.entity.User;
import com.thesis.demouserapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * GET /api/users — list all users (ADMIN only via @PreAuthorize)
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * GET /api/users/me — returns the currently authenticated user's profile
     * Correctly scoped — no IDOR risk here.
     */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication auth) {
        return ResponseEntity.ok(
            userService.getCurrentUser(auth.getName()));
    }

    /**
     * GET /api/users/{id} — returns any user by ID
     *
     * VULNERABILITY-TP [AUTHZ-001] (see UserService.getUserById):
     * Any authenticated user can retrieve any other user's profile.
     * No ownership or admin check performed here or in the service.
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * DELETE /api/users/{id} — deletes any user by ID
     *
     * VULNERABILITY-TP [AUTHZ-002] (see UserService.deleteUser):
     * Any authenticated user can delete any account.
     * Missing @PreAuthorize("hasRole('ADMIN')") annotation.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
