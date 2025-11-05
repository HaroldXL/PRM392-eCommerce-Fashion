package com.example.prm392_finalproject.models;

public class ProductVariant {
    private int id;
    private int productId;
    private String size;
    private String color;
    private int stockQuantity;

    public ProductVariant() {
    }

    public ProductVariant(String size, String color, int stockQuantity) {
        this.size = size;
        this.color = color;
        this.stockQuantity = stockQuantity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
}
