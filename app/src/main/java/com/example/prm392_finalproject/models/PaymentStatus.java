package com.example.prm392_finalproject.models;

public class PaymentStatus {
    private String message;
    private boolean result;
    private String orderId;

    // Getters
    public String getMessage() {
        return message;
    }

    public boolean isResult() {
        return result;
    }

    public String getOrderId() {
        return orderId;
    }

    // Setters (optional, useful if you use Gson)
    public void setMessage(String message) {
        this.message = message;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
