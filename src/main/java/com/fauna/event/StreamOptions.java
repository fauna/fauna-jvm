package com.fauna.event;

import com.fauna.client.RetryStrategy;

import java.time.Duration;
import java.util.Optional;

/**
 * Represents configuration options for a Fauna stream.
 * <p>
 * The {@code StreamOptions} class allows customization of stream behavior, including cursor,
 * retry strategy, start timestamp, status events, and timeout.
 */
public class StreamOptions {

    private final String cursor;
    private final RetryStrategy retryStrategy;
    private final Long startTimestamp;
    private final Boolean statusEvents;
    private final Duration timeout;

    /**
     * Default {@code StreamOptions} instance with defaults.
     */
    public static final StreamOptions DEFAULT = StreamOptions.builder().build();

    /**
     * Constructs a {@code StreamOptions} instance with the specified builder.
     *
     * @param builder The {@link Builder} instance containing the configuration options.
     */
    public StreamOptions(final Builder builder) {
        this.cursor = builder.cursor;
        this.retryStrategy = builder.retryStrategy;
        this.startTimestamp = builder.startTimestamp;
        this.statusEvents = builder.statusEvents;
        this.timeout = builder.timeout;
    }

    /**
     * Retrieves the cursor for the stream.
     *
     * @return An {@link Optional} containing the cursor, or empty if not set.
     */
    public Optional<String> getCursor() {
        return Optional.ofNullable(cursor);
    }

    /**
     * Retrieves the retry strategy for the stream.
     *
     * @return An {@link Optional} containing the retry strategy, or empty if not set.
     */
    public Optional<RetryStrategy> getRetryStrategy() {
        return Optional.ofNullable(retryStrategy);
    }

    /**
     * Retrieves the start timestamp for the stream.
     *
     * @return An {@link Optional} containing the start timestamp, or empty if not set.
     */
    public Optional<Long> getStartTimestamp() {
        return Optional.ofNullable(startTimestamp);
    }

    /**
     * Checks if status events are enabled for the stream.
     *
     * @return An {@link Optional} containing a boolean for status events, or empty if not set.
     */
    public Optional<Boolean> getStatusEvents() {
        return Optional.ofNullable(statusEvents);
    }

    /**
     * Retrieves the timeout duration for the stream.
     *
     * @return An {@link Optional} containing the timeout duration, or empty if not set.
     */
    public Optional<Duration> getTimeout() {
        return Optional.ofNullable(timeout);
    }

    /**
     * Builder class for constructing {@code StreamOptions} instances.
     */
    public static class Builder {
        private String cursor = null;
        private RetryStrategy retryStrategy = null;
        private Long startTimestamp = null;
        private Boolean statusEvents = null;
        private Duration timeout = null;

        /**
         * Sets the cursor for the stream.
         *
         * @param cursor A {@code String} representing the cursor position.
         * @return This {@code Builder} instance.
         */
        public Builder cursor(final String cursor) {
            this.cursor = cursor;
            return this;
        }

        /**
         * Sets the retry strategy for the stream.
         *
         * @param retryStrategy The {@link RetryStrategy} for managing retries.
         * @return This {@code Builder} instance.
         */
        public Builder retryStrategy(final RetryStrategy retryStrategy) {
            this.retryStrategy = retryStrategy;
            return this;
        }

        /**
         * Sets the start timestamp for the stream.
         *
         * @param startTimestamp A {@code long} representing the start timestamp.
         * @return This {@code Builder} instance.
         */
        public Builder startTimestamp(final long startTimestamp) {
            this.startTimestamp = startTimestamp;
            return this;
        }

        /**
         * Enables or disables status events for the stream.
         *
         * @param statusEvents A {@code Boolean} indicating if status events are enabled.
         * @return This {@code Builder} instance.
         */
        public Builder statusEvents(final Boolean statusEvents) {
            this.statusEvents = statusEvents;
            return this;
        }

        /**
         * Sets the timeout duration for the stream.
         *
         * @param timeout A {@link Duration} representing the timeout.
         * @return This {@code Builder} instance.
         */
        public Builder timeout(final Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Builds a new {@code StreamOptions} instance with the configured parameters.
         *
         * @return A new {@code StreamOptions} instance.
         */
        public StreamOptions build() {
            return new StreamOptions(this);
        }
    }

    /**
     * Creates a new {@code Builder} for constructing {@code StreamOptions}.
     *
     * @return A new {@code Builder} instance.
     */
    public static Builder builder() {
        return new Builder();
    }
}
