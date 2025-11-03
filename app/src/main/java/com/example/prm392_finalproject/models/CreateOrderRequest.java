package com.example.prm392_finalproject.models;

import java.util.List;

public class CreateOrderRequest {
    private Order order;
    private List<OrderItem> orderItems;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(Order order, List<OrderItem> orderItems) {
        this.order = order;
        this.orderItems = orderItems;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
}
