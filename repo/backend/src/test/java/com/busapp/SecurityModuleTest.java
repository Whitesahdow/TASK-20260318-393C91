package com.busapp;

import com.busapp.model.UserEntity;
import com.busapp.model.UserRole;
import com.busapp.repository.UserRepository;
import com.busapp.service.AuthService;
import com.busapp.service.RegisterRequest;
import com.busapp.service.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class SecurityModuleTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void whenPasswordShort_thenReturns400() throws Exception {
        String shortPasswordJson = "{\"username\":\"test\", \"password\":\"123\"}";
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(shortPasswordJson))
                .andExpect(status().isBadRequest());
    }

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
