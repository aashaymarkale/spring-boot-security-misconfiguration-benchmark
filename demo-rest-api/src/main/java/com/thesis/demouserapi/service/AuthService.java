package com.thesis.demouserapi.service;

import com.thesis.demouserapi.dto.AuthRequest;
import com.thesis.demouserapi.dto.AuthResponse;
import com.thesis.demouserapi.dto.RegisterRequest;
import com.thesis.demouserapi.entity.Role;
import com.thesis.demouserapi.entity.User;
import com.thesis.demouserapi.repository.UserRepository;
import com.thesis.demouserapi.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                "Email already registered: " + request.getEmail());
        }

        User user = User.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(Role.USER)
            .enabled(true)
            .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
            .token(token)
            .email(user.getEmail())
            .role(user.getRole().name())
            .build();
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() ->
                new IllegalArgumentException("User not found"));

        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
            .token(token)
            .email(user.getEmail())
            .role(user.getRole().name())
            .build();
    }
}
