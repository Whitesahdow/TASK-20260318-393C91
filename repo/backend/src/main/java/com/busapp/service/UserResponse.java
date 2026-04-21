package com.busapp.service;

public class UserResponse {
    private final String username;
    private final String role;

    public UserResponse(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}
