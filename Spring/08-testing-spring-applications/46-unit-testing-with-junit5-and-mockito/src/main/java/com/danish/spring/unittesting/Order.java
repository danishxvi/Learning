package com.danish.spring.unittesting;

public class Order {
    private Long id;
    private final String product;
    private final int quantity;
    private final int totalCents;

    public Order(String product, int quantity, int totalCents) {
        this.product = product;
        this.quantity = quantity;
        this.totalCents = totalCents;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public int getTotalCents() { return totalCents; }
}
