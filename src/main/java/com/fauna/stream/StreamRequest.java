package com.fauna.stream;

import java.util.Optional;

/**
 * This class defines the request body expected by the fauna /stream endpoint.
 */
public class StreamRequest {
    private final String token;
    private final String cursor;
    private final Long start_ts;

    public StreamRequest(String token) {
        this.token = token;
        this.cursor = null;
        this.start_ts = null;
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("token cannot be null or empty");
        }
    }

    public StreamRequest(String token, String cursor) {
        this.token = token;
        this.cursor = cursor;
        this.start_ts = null;
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("token cannot be null or empty");
        }
    }

    public StreamRequest(String token, Long start_ts) {
        this.token = token;
        this.cursor = null;
        this.start_ts = start_ts;
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("token cannot be null or empty");
        }
    }

    public String getToken() {
        return token;
    }

    public Optional<String> getCursor() {
        return Optional.ofNullable(cursor);
    }

    public Optional<Long> getStartTs() {
        return Optional.ofNullable(start_ts);
    }
}
