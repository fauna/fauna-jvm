package com.fauna.query;

import java.time.Duration;
import java.util.Optional;

import static com.fauna.constants.Defaults.DEFAULT_TIMEOUT;

public class QueryOptions {
    private final Boolean linearized;
    private final Boolean typeCheck;
    private final Duration timeout;
    private final QueryTags queryTags;
    private final String traceParent;

    public static QueryOptions DEFAULT = QueryOptions.builder().build();

    public QueryOptions(Builder builder) {
        this.linearized = builder.linearized;
        this.typeCheck = builder.typeCheck;
        this.timeout = builder.timeout;
        this.queryTags = builder.queryTags;
        this.traceParent = builder.traceParent;
    }

    public Optional<Boolean> getLinearized() {
        return Optional.ofNullable(this.linearized);
    }

    public Optional<Boolean> getTypeCheck() {
        return Optional.ofNullable(this.typeCheck);
    }

    public Optional<Long> getTimeoutMillis() {
        return Optional.ofNullable(this.timeout).map(Duration::toMillis);
    }

    public Optional<QueryTags> getQueryTags() {
        return Optional.ofNullable(this.queryTags);
    }

    public Optional<String> getTraceParent() {
        return Optional.ofNullable(this.traceParent);
    }

    public static class Builder {
        public Boolean linearized = null;
        public Boolean typeCheck = null;
        public Duration timeout = DEFAULT_TIMEOUT;
        public QueryTags queryTags = null;
        public String traceParent = null;

        public Builder linearized(boolean linearized) {
            this.linearized = linearized;
            return this;
        }

        public Builder typeCheck(boolean typeCheck) {
            this.typeCheck = typeCheck;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder queryTags(QueryTags queryTags) {
            if (this.queryTags != null) {
                this.queryTags.putAll(queryTags);
            } else {
                this.queryTags = queryTags;
            }
            return this;
        }

        public Builder queryTag(String key, String value) {
            if (this.queryTags == null) {
                this.queryTags = new QueryTags();
            }
            this.queryTags.put(key, value);
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
