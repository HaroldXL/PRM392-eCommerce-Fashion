package com.example.prm392_finalproject.models;

import java.util.List;

public class PaymentInitRequest {
    private int userId;
    private String paymentMethod;
    private String shippingName;
    private String shippingPhone;
    private String shippingAddress;
    private List<PaymentItem> items;

    public PaymentInitRequest(int userId, String paymentMethod, String shippingName,
            String shippingPhone, String shippingAddress, List<PaymentItem> items) {
        this.userId = userId;
        this.paymentMethod = paymentMethod;
        this.shippingName = shippingName;
        this.shippingPhone = shippingPhone;
        this.shippingAddress = shippingAddress;
        this.items = items;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getShippingName() {
        return shippingName;
    }

    public void setShippingName(String shippingName) {
        this.shippingName = shippingName;
    }

    public String getShippingPhone() {
        return shippingPhone;
    }

    public void setShippingPhone(String shippingPhone) {
        this.shippingPhone = shippingPhone;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public List<PaymentItem> getItems() {
        return items;
    }

    public void setItems(List<PaymentItem> items) {
        this.items = items;
    }

    public static class PaymentItem {
        private int productVariantId;
        private int amount;

        public PaymentItem(int productVariantId, int amount) {
            this.productVariantId = productVariantId;
            this.amount = amount;
        }

        public int getProductVariantId() {
            return productVariantId;
        }

        public void setProductVariantId(int productVariantId) {
            this.productVariantId = productVariantId;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }
    }
}
