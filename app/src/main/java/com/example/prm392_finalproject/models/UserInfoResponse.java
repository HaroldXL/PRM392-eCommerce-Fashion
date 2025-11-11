package com.example.prm392_finalproject.models;

public class UserInfoResponse {
    private int id;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private Integer role;
    private String roleName;

    // Getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
}
