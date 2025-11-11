package com.example.prm392_finalproject.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Chat {

    private int id;
    private int customerId;
    private String startedAt;
    private List<ChatMessage> chatContents = new ArrayList<>();
    private UserInfoResponse customer;

    // Constructors
    public Chat() {}

    public Chat(int id, int customerId, String startedAt, List<ChatMessage> chatContents, UserInfoResponse customer) {
        this.id = id;
        this.customerId = customerId;
        this.startedAt = startedAt;
        this.chatContents = chatContents;
        this.customer = customer;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    public List<ChatMessage> getChatContents() {
        return chatContents;
    }

    public void setChatContents(List<ChatMessage> chatContents) {
        this.chatContents = chatContents;
    }

    public UserInfoResponse getCustomer() {
        return customer;
    }

    public void setCustomer(UserInfoResponse customer) {
        this.customer = customer;
    }

}
