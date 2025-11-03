package com.example.prm392_finalproject.models;

public class OrderItem {
    private int productVariantId;
    private double unitPrice;
    private int quantity;

    public OrderItem() {
    }

    public OrderItem(int productVariantId, double unitPrice, int quantity) {
        this.productVariantId = productVariantId;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public int getProductVariantId() {
        return productVariantId;
    }

    public void setProductVariantId(int productVariantId) {
        this.productVariantId = productVariantId;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
