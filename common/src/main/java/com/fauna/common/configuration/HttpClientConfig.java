package com.fauna.common.configuration;

import java.time.Duration;

/**
 * HttpClientConfig encapsulates configuration settings for an HTTP client.
 * It includes settings such as connection timeout and maximum number of connections.
 */
public class HttpClientConfig {

    private final Duration connectTimeout;
    private final int maxConnections;

    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final int DEFAULT_MAX_CONNECTIONS = 20;


    /**
     * Private constructor for HttpClientConfig.
     *
     * @param builder The builder used to create the HttpClientConfig instance.
     */
    private HttpClientConfig(Builder builder) {
        this.connectTimeout = builder.connectTimeout;
        this.maxConnections = builder.maxConnections;
    }

    /**
     * Gets the connection timeout setting.
     *
     * @return A Duration representing the maximum time to wait for a connection to be established.
     * The default is 5 seconds.
     */
    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Gets the maximum number of connections setting.
     *
     * @return An integer representing the maximum number of simultaneous connections.
     * The default is 20.
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    /**
     * Creates a new builder for HttpClientConfig.
     *
     * @return A new instance of Builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for HttpClientConfig. Follows the Builder Design Pattern.
     */
    public static class Builder {

        private Duration connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        private int maxConnections = DEFAULT_MAX_CONNECTIONS;

        /**
         * Sets the connection timeout.
         *
         * @param connectTimeout A Duration representing the connection timeout.
         *                       The default is 5 seconds.
         * @return The current Builder instance.
         */
        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        /**
         * Sets the maximum number of connections.
         *
         * @param maxConnections An integer representing the maximum number of connections.
         *                       The default is 20.
         * @return The current Builder instance.
         */
        public Builder maxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        /**
         * Builds and returns a new HttpClientConfig instance.
         *
         * @return A new instance of HttpClientConfig.
         */
        public HttpClientConfig build() {
            return new HttpClientConfig(this);
        }
    }

}
