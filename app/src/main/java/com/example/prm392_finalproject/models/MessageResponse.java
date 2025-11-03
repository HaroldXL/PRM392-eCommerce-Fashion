package com.example.prm392_finalproject.models;

public class MessageResponse {
    private String message;
    private Boolean verified; // For OTP verification

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }
}
