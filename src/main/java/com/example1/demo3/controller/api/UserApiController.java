package com.example1.demo3.controller.api;


import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example1.demo3.dto.UserRegisterRequest;
import com.example1.demo3.entity.User;
import com.example1.demo3.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
public class UserApiController {
    
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserApiController(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> register(@RequestBody UserRegisterRequest req) {

        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("ユーザー名が既に存在しています");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole("ROLE_" + req.getRole());

        userRepository.save(user);

        return ResponseEntity.ok("OK");
    }
}
