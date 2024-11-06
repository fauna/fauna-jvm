package com.fauna.event;

import java.time.Duration;
import java.util.Optional;

import static com.fauna.constants.Defaults.DEFAULT_TIMEOUT;

public class FeedOptions {
    private final String cursor;
    private final Long startTs;
    private final Integer pageSize;
    private final Duration timeout;

    public static FeedOptions DEFAULT = FeedOptions.builder().build();

    public FeedOptions(String cursor, Long startTs, Integer pageSize,
                       Duration timeout) {
        this.cursor = cursor;
        this.startTs = startTs;
        this.pageSize = pageSize;
        this.timeout = timeout;
        if (cursor != null && startTs != null) {
            throw new IllegalArgumentException(
                    "Only one of cursor, and startTs can be set.");
        }
    }

    public Optional<String> getCursor() {
        return Optional.ofNullable(cursor);
    }

    public Optional<Long> getStartTs() {
        return Optional.ofNullable(startTs);
    }

    public Optional<Integer> getPageSize() {
        return Optional.ofNullable(pageSize);
    }

    public Optional<Duration> getTimeout() {
        return Optional.ofNullable(timeout);
    }

    public static class Builder {
        public String cursor = null;
        public Long startTs = null;
        public Integer pageSize = null;
        public Duration timeout = DEFAULT_TIMEOUT;

        public Builder cursor(String cursor) {
            if (startTs != null) {
                throw new IllegalArgumentException(
                        "Only one of cursor, and startTs can be set.");
            }
            this.cursor = cursor;
            return this;
        }

        public Builder startTs(Long startTs) {
            if (cursor != null) {
                throw new IllegalArgumentException(
                        "Only one of cursor, and startTs can be set.");
            }
            this.startTs = startTs;
            return this;
        }

        public Builder pageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public FeedOptions build() {
            return new FeedOptions(cursor, startTs, pageSize, timeout);
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Return the FeedOptions for the next page, based on the cursor of the given page.
     * <p>
     * This method copies options, like pageSize, and timeout.
     *
     * @param page The current, or latest page.
     * @return A new FeedOptions instance.
     */
    public FeedOptions nextPage(FeedPage<?> page) {
        FeedOptions.Builder builder =
                FeedOptions.builder().cursor(page.getCursor());
        // Do not set or copy startTs, because we are using cursor.
        getPageSize().ifPresent(builder::pageSize);
        getTimeout().ifPresent(builder::timeout);
        return builder.build();
    }
}
