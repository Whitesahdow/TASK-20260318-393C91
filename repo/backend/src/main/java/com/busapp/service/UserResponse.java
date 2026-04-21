package com.busapp.service;

import com.busapp.model.UserRole;

public class UserResponse {
    private final String username;
    private final UserRole role;

    public UserResponse(String username, UserRole role) {
        this.username = username;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }
}
