package com.example1.demo3.unit.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.example1.demo3.controller.LoginController;

class LoginControllerTest {

    private final LoginController loginController = new LoginController();

    @Test
    void showLoginPage_shouldReturnLoginView() {
        String viewName = loginController.showLoginPage();

        assertEquals("login", viewName);
    }

    @Test
    void showLoginPage_shouldNotThrowException() {
        assertDoesNotThrow(() -> loginController.showLoginPage());
    }
}
