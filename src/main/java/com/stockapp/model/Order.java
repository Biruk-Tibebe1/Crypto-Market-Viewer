package com.stockapp.model;

import java.time.LocalDateTime;

public class Order {
    public String user;
    public String symbol;
    public String side;
    public double quantity;
    public double price;
    public String status;
    public LocalDateTime createdAt;

    public Order(String user, String symbol, String side, double quantity, double price, String status, LocalDateTime createdAt) {
        this.user = user;
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
    }
}
