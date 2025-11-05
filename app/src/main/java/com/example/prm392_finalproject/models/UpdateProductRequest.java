package com.example.prm392_finalproject.models;

import java.util.List;

public class UpdateProductRequest {
    private String name;
    private String description;
    private String imageUrl;
    private double price;
    private List<ProductVariant> productVariants;

    public UpdateProductRequest() {
    }

    public UpdateProductRequest(String name, String description, String imageUrl,
            double price, List<ProductVariant> productVariants) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.productVariants = productVariants;
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
