package com.example.prm392_finalproject.models;

import java.util.List;

public class CreateProductRequest {
    private int categoryId;
    private String name;
    private String description;
    private String imageUrl;
    private double price;
    private List<ProductVariant> productVariants;

    public CreateProductRequest() {
    }

    public CreateProductRequest(int categoryId, String name, String description,
            String imageUrl, double price, List<ProductVariant> productVariants) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.productVariants = productVariants;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<ProductVariant> getProductVariants() {
        return productVariants;
    }

    public void setProductVariants(List<ProductVariant> productVariants) {
        this.productVariants = productVariants;
    }
}
