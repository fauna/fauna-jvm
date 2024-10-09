package com.fauna.stream;

import com.fauna.query.StreamTokenResponse;

import java.time.Duration;
import java.util.Optional;

/**
 * This class defines the request body expected by the fauna /stream endpoint.
 */
public class StreamRequest {
    private final String token;
    private final String cursor;
    private final Long startTs;
    private final Duration timeout;

    StreamRequest(String token, String cursor, Long startTs, Duration timeout) {
        this.token = token;
        this.cursor = cursor;
        this.startTs = startTs;
        this.timeout = timeout;
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("token cannot be null or empty");
        }
        if (cursor != null && startTs != null) {
            throw new IllegalArgumentException("Only one of cursor, or start_ts can be set.");
        }
    }

    public static StreamRequest fromTokenResponse(StreamTokenResponse tokenResponse) {
        return new StreamRequest(tokenResponse.getToken(), null, null, null);
    }

    public static class Builder {
        final String token;
        String cursor = null;
        Long startTs = null;
        Duration timeout = null;

        /**
         * Return a new StreamRequest.Builder instance with the given token.
         * @param token     A Fauna Stream token.
         */
        public Builder(String token) {
            this.token = token;
        }

        /**
         * Return the current Builder instance with the given cursor.
         * @param cursor    A Fauna Stream cursor.
         * @return          The current Builder instance.
         * @throws          IllegalArgumentException If startTs has already been set.
         */
        public Builder cursor(String cursor) {
            if (this.startTs != null) {
                throw new IllegalArgumentException("only one of cursor, or startTs can be set.");
            }
            this.cursor = cursor;
            return this;
        }

        /**
         * Return the current Builder instance with the given start timestamp.
         * @param startTs   A timestamp to start the stream at.
         * @return          The current Builder instance.
         * @throws          IllegalArgumentException If startTs has already been set.
         */
        public Builder startTs(Long startTs) {
            if (this.cursor != null) {
                throw new IllegalArgumentException("only one of cursor, or startTs can be set.");
            }
            this.startTs = startTs;
            return this;
        }

        /**
         * Return the current builder instance with the given timeout.
         * This timeout is the HTTP client timeout that is passed to java.net.http.HttpRequest.Builder.
         * By testing, it seems that the client waits for the first byte, not the last byte of the response.
         * Therefore, it's a timeout on the stream being opened, but the stream can stay open indefinitely.
         * @param timeout   A Duration representing the timeout.
         * @return          The current Builder instance.
         */
        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public StreamRequest build() {
            return new StreamRequest(token, cursor, startTs, timeout);
        }

    }

    /**
     * Create a new StreamRequest.Builder instance.
     * @param token The Fauna Stream token to use.
     * @return  A new StreamRequest.Builder instance.
     */
    public static Builder builder(String token) {
        return new Builder(token);
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

    public Optional<Duration> getTimeout() {
        return Optional.ofNullable(timeout);
    }
}
