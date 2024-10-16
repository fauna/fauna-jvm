package com.fauna.feed;

import java.util.Optional;

public class FeedRequest {
    private final String token;
    private final String cursor;
    private final Long startTs;
    private final Integer pageSize;

    public FeedRequest(final String token, final String cursor, final Long startTs, final Integer pageSize) {
        this.token = token;
        this.cursor = cursor;
        this.startTs = startTs;
        this.pageSize = pageSize;
    }

    public String getToken() {
        return token;
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

    public static class Builder {
        public final String token;
        public String cursor;
        public Long startTs;
        public Integer pageSize;

        public Builder(final String token) {
            this.token = token;
        }

        public Builder cursor(final String cursor) {
            this.cursor = cursor;
            return this;
        }

        public Builder startTs(final Long startTs) {
            this.startTs = startTs;
            return this;
        }

        public Builder pageSize(final int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public FeedRequest build() {
            return new FeedRequest(token, cursor, startTs, pageSize);
        }
    }

    public static Builder builder(String token) {
        return new Builder(token);
    }
}
