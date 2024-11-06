package com.fauna.event;

import com.fauna.client.RetryStrategy;

import java.time.Duration;
import java.util.Optional;

public class StreamOptions {

    private final String cursor;
    private final RetryStrategy retryStrategy;
    private final Long startTimestamp;
    private final Boolean statusEvents;
    private final Duration timeout;

    public static final StreamOptions DEFAULT = StreamOptions.builder().build();

    public StreamOptions(Builder builder) {
        this.cursor = builder.cursor;
        this.retryStrategy = builder.retryStrategy;
        this.startTimestamp = builder.startTimestamp;
        this.statusEvents = builder.statusEvents;
        this.timeout = builder.timeout;
    }

    public Optional<String> getCursor() {
        return Optional.ofNullable(cursor);
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

    public Optional<Duration> getTimeout() {
        return Optional.ofNullable(timeout);
    }


    public static class Builder {
        public String cursor = null;
        public RetryStrategy retryStrategy = null;
        public Long startTimestamp = null;
        public Boolean statusEvents = null;
        public Duration timeout = null;

        public Builder cursor(String cursor) {
            this.cursor = cursor;
            return this;
        }

        public Builder retryStrategy(RetryStrategy retryStrategy) {
            this.retryStrategy = retryStrategy;
            return this;
        }

        public Builder startTimestamp(long startTimestamp) {
            this.startTimestamp = startTimestamp;
            return this;
        }

        public Builder statusEvents(Boolean statusEvents) {
            this.statusEvents = statusEvents;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
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
