package com.example.prm392_finalproject.models;

public class PaymentStatus {
    private String message;
    private boolean result;
    private String orderId;
    private Double amount;

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

    public Double getAmount() {
        return amount;
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

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
