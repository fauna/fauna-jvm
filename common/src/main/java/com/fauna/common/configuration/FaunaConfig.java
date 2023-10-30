package com.fauna.common.configuration;

import java.time.Duration;
import java.util.Map;

public class FaunaConfig {

    private final String endpoint;
    private final String secret;
    private final Boolean linearized;
    private final Boolean typeCheck;
    private final Duration queryTimeout;
    private final String traceParent;
    private final Map<String, String> queryTags;

//    private Integer maxContentionRetries;
//    private int maxAttempts;
//    private int maxBackoff;

    private static final Duration DEFAULT_QUERY_TIMEOUT = Duration.ofSeconds(5);

    private FaunaConfig(Builder builder) {
        this.endpoint = builder.endpoint;
        this.secret = builder.secret;
        this.queryTimeout = builder.queryTimeout;
        this.linearized = builder.linearized;
        this.typeCheck = builder.typeCheck;
        this.queryTags = builder.queryTags;
        this.traceParent = builder.traceParent;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getSecret() {
        return secret;
    }

    public Duration getQueryTimeout() {
        return queryTimeout;
    }

    public Boolean getLinearized() {
        return linearized;
    }

    public Boolean getTypeCheck() {
        return typeCheck;
    }

    public Map<String, String> getQueryTags() {
        return queryTags;
    }

    public String getTraceParent() {
        return traceParent;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String endpoint;
        private String secret;
        private Duration queryTimeout = DEFAULT_QUERY_TIMEOUT;
        private Boolean linearized;
        private Boolean typeCheck;
        private Map<String, String> queryTags;
        private String traceParent;

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder secret(String secret) {
            this.secret = secret;
            return this;
        }

        public Builder queryTimeout(Duration queryTimeout) {
            this.queryTimeout = queryTimeout;
            return this;
        }

        public Builder linearized(Boolean linearized) {
            this.linearized = linearized;
            return this;
        }

        public Builder typeCheck(Boolean typeCheck) {
            this.typeCheck = typeCheck;
            return this;
        }

        public Builder queryTags(Map<String, String> queryTags) {
            this.queryTags = queryTags;
            return this;
        }

        public Builder traceParent(String traceParent) {
            this.traceParent = traceParent;
            return this;
        }

        public FaunaConfig build() {
            return new FaunaConfig(this);
        }
    }


}
