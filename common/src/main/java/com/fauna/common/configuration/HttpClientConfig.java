package com.fauna.common.configuration;

import java.time.Duration;

public class HttpClientConfig {

    private final Duration connectTimeout;
    private final int maxConnections;

    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final int DEFAULT_MAX_CONNECTIONS = 20;


    private HttpClientConfig(Builder builder) {
        this.connectTimeout = builder.connectTimeout;
        this.maxConnections = builder.maxConnections;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Duration connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        private int maxConnections = DEFAULT_MAX_CONNECTIONS;

        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder maxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        public HttpClientConfig build() {
            return new HttpClientConfig(this);
        }
    }

}
