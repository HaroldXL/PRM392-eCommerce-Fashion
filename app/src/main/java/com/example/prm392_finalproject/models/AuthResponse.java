package com.example.prm392_finalproject.models;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    private String email;

    @SerializedName("accessToken")
    private String accessToken;

    @SerializedName("expiresIn")
    private int expiresIn;

    private int role;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    // Helper methods for backward compatibility
    public boolean isSuccess() {
        return accessToken != null && !accessToken.isEmpty();
    }

    public String getToken() {
        return accessToken;
    }

    public boolean isCustomer() {
        return role == 2;
    }

    public boolean isAdmin() {
        return role == 1;
    }

    public boolean isManager() {
        return role == 3;
    }
}
