package com.example1.demo3.dto;

public class UserRegisterRequest {
    private String username;
    private String password;
    private String role;

    public UserRegisterRequest() {
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
