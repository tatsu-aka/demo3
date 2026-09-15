package com.example1.demo3.unit.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example1.demo3.controller.api.UserApiController;
import com.example1.demo3.dto.UserRegisterRequest;
import com.example1.demo3.entity.User;
import com.example1.demo3.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserApiControllerTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserApiController userApiController;

    @Test
    void register_shouldSaveNormalizedRole() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("admin");
        request.setPassword("password");
        request.setRole("ADMIN");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encoded");

        ResponseEntity<?> response = userApiController.register(request);

        assertEquals(200, response.getStatusCode().value());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("ADMIN", userCaptor.getValue().getRole());
        assertEquals("encoded", userCaptor.getValue().getPassword());
    }

    @Test
    void register_shouldRejectUnknownRole() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("user");
        request.setPassword("password");
        request.setRole("SUPER_ADMIN");

        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());

        ResponseEntity<?> response = userApiController.register(request);

        assertEquals(400, response.getStatusCode().value());
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
        verify(passwordEncoder, never()).encode(org.mockito.ArgumentMatchers.anyString());
    }
}