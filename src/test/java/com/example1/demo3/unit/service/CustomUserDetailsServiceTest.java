package com.example1.demo3.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example1.demo3.entity.User;
import com.example1.demo3.repository.UserRepository;
import com.example1.demo3.service.CustomUserDetailsService;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    //正常系
    @Test
    void loadUserByUsername_shouldReturnUserDetails() {

        //準備
        User user = new User();
        user.setUsername("Taro");
        user.setPassword("encodedPass");
        user.setRole("ADMIN");

        when(userRepository.findByUsername("Taro"))
                .thenReturn(Optional.of(user));

        //実行
        UserDetails details = customUserDetailsService.loadUserByUsername("Taro");

        //検証
        assertNotNull(details);
        assertEquals("Taro", details.getUsername());
        assertEquals("encodedPass", details.getPassword());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    //異常系　ユーザーが見つからない
    @Test
    void loadUserByUsername_shouldThrowExceptionWhenUserNotFound() {

        //準備
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        //実行
        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername("unknown"));
    }
}
