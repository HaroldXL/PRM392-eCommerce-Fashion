package com.example.prm392_finalproject.models;

public class ChatMessage {
    private int id;
    private String chatContent;
    private long no;
    private String sentAt;
    private String status;
    private int chatId;
    private Integer staffId;
    private UserInfoResponse from;

    // Getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getChatContent() { return chatContent; }
    public void setChatContent(String chatContent) { this.chatContent = chatContent; }

    public long getNo() { return no; }
    public void setNo(long no) { this.no = no; }

    public String getSentAt() { return sentAt; }
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getChatId() { return chatId; }
    public void setChatId(int chatId) { this.chatId = chatId; }

    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }

    public UserInfoResponse getFrom() { return from; }
    public void setFrom(UserInfoResponse from) { this.from = from; }

    // Convenience method to check if this message is from current user
    public boolean isUser(int currentUserId) {
        return from != null && from.getId() == currentUserId;
    }
}
