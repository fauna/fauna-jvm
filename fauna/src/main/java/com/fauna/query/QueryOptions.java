package com.fauna.query;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

public class QueryOptions {
    private final Boolean linearized;
    private final Boolean typeCheck;
    private final Duration timeout;
    private final Map<String, String> queryTags;
    private final String traceParent;

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);
    public static QueryOptions DEFAULT = QueryOptions.builder().build();

    public QueryOptions(Builder builder) {
        this.linearized = builder.linearized;
        this.typeCheck = builder.typeCheck;
        this.timeout = builder.timeout;
        this.queryTags = builder.queryTags;
        this.traceParent = builder.traceParent;
    }

    public Optional<Boolean> isLinearized() {
        return Optional.ofNullable(this.linearized);
    }

    public Optional<Boolean> getTypeCheck() {
        return Optional.ofNullable(this.typeCheck);
    }

    public Optional<Duration> getTimeout() {
        return Optional.ofNullable(this.timeout);
    }

    public Optional<Map<String, String>> getQueryTags() {
        return Optional.ofNullable(this.queryTags);
    }

    public Optional<String> getTraceParent() {
        return Optional.ofNullable(this.traceParent);
    }

    public static class Builder {
        public Boolean linearized = null;
        public Boolean typeCheck = null;
        public Duration timeout = DEFAULT_TIMEOUT;
        public Map<String, String> queryTags;
        public String traceParent = null;

        public Builder linearized(boolean linearized) {
            this.linearized = linearized;
            return this;
        }

        public Builder typeCheck(boolean typeCheck) {
            this.typeCheck = linearized;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder queryTags(Map<String, String> tags) {
            this.queryTags = tags;
            return this;
        }

        public Builder traceParent(String traceParent) {
            this.traceParent = traceParent;
            return this;
        }

        public QueryOptions build() {
            return new QueryOptions(this);
        }

    }

    public static Builder builder() {
        return new Builder();
    }

}
