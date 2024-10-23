package com.fauna.perf.model;

public class Manufacturer {
    private String name;
    private String location;

    public Manufacturer() {
        // Default constructor if needed for instantiation via reflection or other methods
    }

    // Getters for all properties
    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }
}
