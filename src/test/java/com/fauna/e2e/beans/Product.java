package com.fauna.e2e.beans;

public class Product {
    private String name;
    private int quantity;

    public Product() {
    }

    public Product(String id, String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() {
        return this.name;
    }

    public int getQuantity() {
        return this.quantity;
    }
}

