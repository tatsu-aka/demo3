package com.example1.demo3.unit.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.example1.demo3.controller.UserRegisterScreenController;

class UserRegisterScreenControllerTest {

    private final UserRegisterScreenController userRegisterScreenController = new UserRegisterScreenController();

    @Test
    void showUserRegisterPage_shouldReturnUserRegisterView() {
        String viewName = userRegisterScreenController.showUserRegisterPage();

        assertEquals("user-new", viewName);
    }

    @Test
    void showUserRegisterPage_shouldReturnSameViewWhenCalledRepeatedly() {
        assertEquals("user-new", userRegisterScreenController.showUserRegisterPage());
        assertEquals("user-new", userRegisterScreenController.showUserRegisterPage());
    }

    @Test
    void showUserRegisterPage_shouldNotThrowException() {
        assertDoesNotThrow(() -> userRegisterScreenController.showUserRegisterPage());
    }
}