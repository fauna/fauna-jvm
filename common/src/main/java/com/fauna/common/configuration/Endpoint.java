package com.fauna.common.configuration;

/**
 * Endpoint enumerates the various endpoints available for connecting to Fauna.
 * It includes the default cloud endpoint and a local endpoint for development purposes.
 */
public enum Endpoint {

    /**
     * The default cloud endpoint for connecting to Fauna.
     */
    DEFAULT("https://db.fauna.com"),

    /**
     * A local endpoint for connecting to a Fauna instance running on localhost.
     */
    LOCAL("http://localhost:8443");

    private final String stringValue;

    /**
     * Constructs a new Endpoint enum instance.
     *
     * @param stringValue The string representation of the endpoint URL.
     */
    Endpoint(String stringValue) {
        this.stringValue = stringValue;
    }

    /**
     * Returns the string representation of the endpoint URL.
     *
     * @return A string representing the endpoint URL.
     */
    @Override
    public String toString() {
        return this.stringValue;
    }

}
