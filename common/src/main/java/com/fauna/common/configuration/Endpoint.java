package com.fauna.common.configuration;

public enum Endpoint {

    DEFAULT("https://db.fauna.com"),
    LOCAL("http://localhost:8443");

    private final String stringValue;

    Endpoint(String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public String toString() {
        return this.stringValue;
    }

}
