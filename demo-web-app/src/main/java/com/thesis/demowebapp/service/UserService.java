package com.thesis.demowebapp.service;

import com.thesis.demowebapp.entity.Note;
import com.thesis.demowebapp.entity.Role;
import com.thesis.demowebapp.entity.User;
import com.thesis.demowebapp.repository.NoteRepository;
import com.thesis.demowebapp.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Seed some demo data on startup so the app is usable
     * immediately without a registration flow.
     */
    @PostConstruct
    public void seedData() {
        User alice = userRepository.save(User.builder()
            .username("alice")
            .password(passwordEncoder.encode("password123"))
            .displayName("Alice")
            .role(Role.USER)
            .enabled(true)
            .build());

        User bob = userRepository.save(User.builder()
            .username("bob")
            .password(passwordEncoder.encode("password123"))
            .displayName("Bob")
            .role(Role.USER)
            .enabled(true)
            .build());

        userRepository.save(User.builder()
            .username("admin")
            .password(passwordEncoder.encode("adminpass123"))
            .displayName("Administrator")
            .role(Role.ADMIN)
            .enabled(true)
            .build());

        noteRepository.save(Note.builder()
            .title("Alice's private note")
            .content("This note should only be visible to Alice.")
            .ownerId(alice.getId())
            .build());

        noteRepository.save(Note.builder()
            .title("Bob's private note")
            .content("This note should only be visible to Bob.")
            .ownerId(bob.getId())
            .build());
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() ->
                new IllegalArgumentException("User not found"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
