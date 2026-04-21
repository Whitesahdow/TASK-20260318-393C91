package com.busapp.service;

import com.busapp.model.UserEntity;
import com.busapp.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    public AuthService(UserRepository userRepository, BCryptPasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    public UserResponse login(String username, String password) {
        if (password == null || password.length() < 8) {
            throw new SecurityException("Password complexity requirement not met (Min 8 chars).");
        }

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new SecurityException("Invalid credentials"));

        if (!encoder.matches(password, user.getPasswordHash())) {
            throw new SecurityException("Invalid credentials");
        }

        return new UserResponse(user.getUsername(), user.getRole());
    }
}
