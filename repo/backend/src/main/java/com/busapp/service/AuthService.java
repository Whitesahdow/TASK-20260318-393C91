package com.busapp.service;

import com.busapp.model.UserEntity;
import com.busapp.model.UserRole;
import com.busapp.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder encoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        validateRegistrationRequest(request);

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateException("Username already exists.");
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setPasswordHash(encoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        userRepository.save(user);

        return new UserResponse(user.getUsername(), user.getRole());
    }

    public UserResponse login(String username, String password) {
        if (password == null || password.length() < 8) {
            throw new SecurityException("Password complexity requirement not met (Min 8 chars).");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new SecurityException("Invalid credentials"));

        return new UserResponse(user.getUsername(), user.getRole());
    }

    private void validateRegistrationRequest(RegisterRequest request) {
        if (request == null) {
            throw new ValidationException("Registration payload is required.");
        }

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new ValidationException("Username is required.");
        }

        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new ValidationException("Password complexity failed: Minimum 8 characters required.");
        }

        if (request.getRole() == null) {
            throw new ValidationException("Role is required.");
        }
    }
}
