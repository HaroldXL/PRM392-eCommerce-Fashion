package com.example.prm392_finalproject.models;

public class SendMessageRequest {
    private int customerId;
    private String message;

    public SendMessageRequest(int customerId, String message) {
        this.customerId = customerId;
        this.message = message;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
