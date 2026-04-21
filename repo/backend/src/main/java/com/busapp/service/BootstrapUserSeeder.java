package com.busapp.service;

import com.busapp.model.UserEntity;
import com.busapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BootstrapUserSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    public BootstrapUserSeeder(UserRepository userRepository, BCryptPasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        userRepository.findByUsername("admin").orElseGet(() -> {
            UserEntity user = new UserEntity();
            user.setUsername("admin");
            user.setPasswordHash(encoder.encode("admin1234"));
            user.setRole("ADMIN");
            return userRepository.save(user);
        });
    }
}
