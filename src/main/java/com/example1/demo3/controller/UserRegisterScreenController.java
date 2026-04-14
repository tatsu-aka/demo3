package com.example1.demo3.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserRegisterScreenController {
    
    @GetMapping("/users/new")
    public String showUserRegisterPage() {
        return "user-new";
    }
}
