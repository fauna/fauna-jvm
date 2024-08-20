package com.fauna.query;

import com.fauna.client.RetryStrategy;

import java.util.Optional;

public class StreamOptions {

    private final RetryStrategy retryStrategy;
    private final Long startTimestamp;
    private final Boolean statusEvents;

    public StreamOptions(Builder builder) {
        this.retryStrategy = builder.retryStrategy;
        this.startTimestamp = builder.startTimestamp;
        this.statusEvents = builder.statusEvents;
    }

    public Optional<RetryStrategy> getRetryStrategy() {
        return Optional.ofNullable(retryStrategy);
    }

    public Optional<Long> getStartTimestamp() {
        return Optional.ofNullable(startTimestamp);

    }

    public Optional<Boolean>  getStatusEvents() {
        return Optional.ofNullable(statusEvents);
    }


    public static class Builder {
        public RetryStrategy retryStrategy = null;
        public Long startTimestamp = null;
        public Boolean statusEvents = null;

        public Builder withRetryStrategy(RetryStrategy retryStrategy) {
            this.retryStrategy = retryStrategy;
            return this;
        }

        public Builder withStartTimestamp(long startTimestamp) {
            this.startTimestamp = startTimestamp;
            return this;
        }

        public Builder withStatusEvents(Boolean statusEvents) {
            this.statusEvents = statusEvents;
            return this;
        }

        public StreamOptions build() {
            return new StreamOptions(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
