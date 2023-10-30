package com.fauna.common.configuration;

public enum JvmDriver {
    JAVA("Java"),
    SCALA("Scala");

    private final String stringValue;

    JvmDriver(String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public String toString() {
        return this.stringValue;
    }
}