package com.busapp.service;

import com.busapp.model.UserEntity;
import com.busapp.model.UserRole;
import com.busapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_WithShortPassword_ShouldThrowException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user");
        request.setPassword("12345");
        request.setRole(UserRole.PASSENGER);

        assertThrows(ValidationException.class, () -> authService.register(request));
    }

    @Test
    void register_ShouldHashPasswordBeforeSaving() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user");
        request.setPassword("validPassword123");
        request.setRole(UserRole.PASSENGER);

        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed_val");

        authService.register(request);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertEquals("hashed_val", captor.getValue().getPasswordHash());
        assertEquals(UserRole.PASSENGER, captor.getValue().getRole());
    }
}