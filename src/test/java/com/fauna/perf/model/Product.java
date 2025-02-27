package com.fauna.perf.model;

import com.fauna.types.DocumentRef;

public class Product {
    private String name;
    private String category;
    private final int price = 0;
    private final int quantity = 0;
    private final boolean inStock = false;
    private DocumentRef manufacturerRef;

    public Product() {
    }

    // Getters for all properties
    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isInStock() {
        return inStock;
    }

    public DocumentRef getManufacturerRef() {
        return manufacturerRef;
    }
}
