package com.fauna.event;

import java.time.Duration;
import java.util.Optional;

import static com.fauna.constants.Defaults.DEFAULT_TIMEOUT;

/**
 * Represents the options for configuring an <a href="https://docs.fauna.com/fauna/current/reference/cdc/#event-streaming">event feed</a> request in Fauna.
 * <p>
 * The {@code FeedOptions} class provides configuration parameters such as cursor,
 * start timestamp, page size, and timeout for retrieving feeds from Fauna.
 */
public class FeedOptions {

    private final String cursor;
    private final Long startTs;
    private final Integer pageSize;
    private final Duration timeout;

    /**
     * The default {@code FeedOptions} instance with default settings.
     */
    public static final FeedOptions DEFAULT = FeedOptions.builder().build();

    /**
     * Constructs a new {@code FeedOptions} with the specified parameters.
     *
     * @param cursor   A {@code String} representing the <a href="https://docs.fauna.com/fauna/current/reference/cdc/#get-events-after-a-specific-cursor">cursor</a> in the feed. Cannot be provided with a
     *                 {@code startTs}.
     * @param startTs  A {@code Long} representing the start timestamp for the feed. Represents a time in microseconds since the Unix epoch. Cannot be provided with a
     *                 {@code cursor}.
     * @param pageSize An {@code Integer} specifying the maximum number of
*                      events returned per page. Must be in the range 1 to 16000
*                      (inclusive). Defaults to 16.
     * @param timeout  A {@code Duration} specifying the timeout for the feed request.
     * @throws IllegalArgumentException if both {@code cursor} and {@code startTs} are set.
     */
    public FeedOptions(final String cursor, final Long startTs, final Integer pageSize,
                       final Duration timeout) {
        this.cursor = cursor;
        this.startTs = startTs;
        this.pageSize = pageSize;
        this.timeout = timeout;
        if (cursor != null && startTs != null) {
            throw new IllegalArgumentException(
                    "Only one of cursor and startTs can be set.");
        }
    }

    /**
     * Retrieves the cursor.
     *
     * @return An {@link Optional} containing the cursor, or empty if not set.
     */
    public Optional<String> getCursor() {
        return Optional.ofNullable(cursor);
    }

    /**
     * Retrieves the start timestamp.
     *
     * @return An {@link Optional} containing the start timestamp, or empty if not set.
     */
    public Optional<Long> getStartTs() {
        return Optional.ofNullable(startTs);
    }

    /**
     * Retrieves the page size.
     *
     * @return An {@link Optional} containing the page size, or empty if not set.
     */
    public Optional<Integer> getPageSize() {
        return Optional.ofNullable(pageSize);
    }

    /**
     * Retrieves the timeout duration.
     *
     * @return An {@link Optional} containing the timeout duration, or empty if not set.
     */
    public Optional<Duration> getTimeout() {
        return Optional.ofNullable(timeout);
    }

    /**
     * Builder class for constructing {@code FeedOptions} instances.
     */
    public static class Builder {
        private String cursor = null;
        private Long startTs = null;
        private Integer pageSize = null;
        private Duration timeout = DEFAULT_TIMEOUT;

        /**
         * Sets the cursor.
         *
         * @param cursor A {@code String} representing the cursor.
         * @return This {@code Builder} instance.
         * @throws IllegalArgumentException if {@code startTs} is already set.
         */
        public Builder cursor(final String cursor) {
            if (startTs != null) {
                throw new IllegalArgumentException(
                        "Only one of cursor and startTs can be set.");
            }
            this.cursor = cursor;
            return this;
        }

        /**
         * Sets the start timestamp.
         *
         * @param startTs A {@code Long} representing the start timestamp.
         * @return This {@code Builder} instance.
         * @throws IllegalArgumentException if {@code cursor} is already set.
         */
        public Builder startTs(final Long startTs) {
            if (cursor != null) {
                throw new IllegalArgumentException(
                        "Only one of cursor and startTs can be set.");
            }
            this.startTs = startTs;
            return this;
        }

        /**
         * Sets the page size.
         *
         * @param pageSize An {@code Integer} specifying the number of items per page.
         * @return This {@code Builder} instance.
         */
        public Builder pageSize(final Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        /**
         * Sets the timeout duration.
         *
         * @param timeout A {@code Duration} specifying the timeout for the feed request.
         * @return This {@code Builder} instance.
         */
        public Builder timeout(final Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Builds a new {@code FeedOptions} instance with the configured parameters.
         *
         * @return A new {@code FeedOptions} instance.
         * @throws IllegalArgumentException if both {@code cursor} and {@code startTs} are set.
         */
        public FeedOptions build() {
            return new FeedOptions(cursor, startTs, pageSize, timeout);
        }
    }

    /**
     * Creates a new {@code Builder} for constructing {@code FeedOptions}.
     *
     * @return A new {@code Builder} instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the {@code FeedOptions} for the next page, based on the cursor of the given page.
     * <p>
     * This method copies options like page size and timeout, but does not set or copy {@code startTs},
     * because it uses the cursor.
     *
     * @param page The current or latest {@code FeedPage}.
     * @return A new {@code FeedOptions} instance configured for the next page.
     */
    public FeedOptions nextPage(final FeedPage<?> page) {
        FeedOptions.Builder builder = FeedOptions.builder().cursor(page.getCursor());
        // Do not set or copy startTs, because we are using cursor.
        getPageSize().ifPresent(builder::pageSize);
        getTimeout().ifPresent(builder::timeout);
        return builder.build();
    }
}
