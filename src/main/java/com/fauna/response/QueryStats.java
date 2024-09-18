package com.fauna.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fauna.constants.ResponseFields;
import com.fauna.exception.ClientResponseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.core.JsonToken.FIELD_NAME;
import static com.fasterxml.jackson.core.JsonToken.START_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.VALUE_NULL;
import static com.fasterxml.jackson.core.JsonToken.VALUE_STRING;

public final class QueryStats {

    public final int computeOps;

    public final int readOps;

    public final int writeOps;

    public final int queryTimeMs;

    public final int processingTimeMs;

    public final int contentionRetries;

    public final int storageBytesRead;

    public final int storageBytesWrite;

    public final List<String> rateLimitsHit;

    private String stringValue = null;

    @JsonCreator
    public QueryStats(
            @JsonProperty(ResponseFields.STATS_COMPUTE_OPS_FIELD_NAME) int computeOps,
            @JsonProperty(ResponseFields.STATS_READ_OPS) int readOps,
            @JsonProperty(ResponseFields.STATS_WRITE_OPS) int writeOps,
            @JsonProperty(ResponseFields.STATS_QUERY_TIME_MS) int queryTimeMs,
            @JsonProperty(ResponseFields.STATS_CONTENTION_RETRIES) int contentionRetries,
            @JsonProperty(ResponseFields.STATS_STORAGE_BYTES_READ) int storageBytesRead,
            @JsonProperty(ResponseFields.STATS_STORAGE_BYTES_WRITE) int storageBytesWrite,
            @JsonProperty(ResponseFields.STATS_PROCESSING_TIME_MS) int processingTimeMs,
            @JsonProperty(ResponseFields.STATS_RATE_LIMITS_HIT) List<String> rateLimitsHit) {
        this.computeOps = computeOps;
        this.readOps = readOps;
        this.writeOps = writeOps;
        this.queryTimeMs = queryTimeMs;
        this.contentionRetries = contentionRetries;
        this.storageBytesRead = storageBytesRead;
        this.storageBytesWrite = storageBytesWrite;
        this.processingTimeMs = processingTimeMs;
        this.rateLimitsHit = rateLimitsHit != null ? rateLimitsHit : List.of();
    }

    static class Builder {
        int computeOps;
        int readOps;
        int writeOps;
        int queryTimeMs;
        int contentionRetries;
        int storageBytesRead;
        int storageBytesWrite;
        int processingTimeMs;
        List<String> rateLimitsHit;

        Builder computeOps(int computeOps) {
            this.computeOps = computeOps;
            return this;
        }

        Builder readOps(int readOps) {
            this.readOps = readOps;
            return this;
        }

        Builder writeOps(int writeOps) {
            this.writeOps = writeOps;
            return this;
        }

        Builder queryTimeMs(int queryTimeMs) {
            this.queryTimeMs = queryTimeMs;
            return this;
        }

        Builder contentionRetries(int contentionRetries) {
            this.contentionRetries = contentionRetries;
            return this;
        }

        Builder storageBytesRead(int storageBytesRead) {
            this.storageBytesRead = storageBytesRead;
            return this;
        }

        Builder storageBytesWrite(int storageBytesWrite) {
            this.storageBytesWrite = storageBytesWrite;
            return this;
        }

        Builder processingTimeMs(int processingTimeMs) {
            this.processingTimeMs = processingTimeMs;
            return this;
        }

        Builder rateLimitsHit(List<String> rateLimitsHit) {
            this.rateLimitsHit = rateLimitsHit;
            return this;
        }

        QueryStats build() {
            return new QueryStats(computeOps, readOps, writeOps, queryTimeMs, contentionRetries, storageBytesRead, storageBytesWrite, processingTimeMs, rateLimitsHit);
        }

    }

    static Builder builder() {
        return new Builder();
    }

    static Builder parseField(Builder builder, JsonParser parser) throws IOException {
        String fieldName = parser.getValueAsString();
        switch (fieldName) {
            case ResponseFields.STATS_COMPUTE_OPS_FIELD_NAME:
                return builder.computeOps(parser.nextIntValue(0));
            case ResponseFields.STATS_READ_OPS:
                return builder.readOps(parser.nextIntValue(0));
            case ResponseFields.STATS_WRITE_OPS:
                return builder.writeOps(parser.nextIntValue(0));
            case ResponseFields.STATS_QUERY_TIME_MS:
                return builder.queryTimeMs(parser.nextIntValue(0));
            case ResponseFields.STATS_PROCESSING_TIME_MS:
                return builder.processingTimeMs(parser.nextIntValue(0));
            case ResponseFields.STATS_CONTENTION_RETRIES:
                return builder.contentionRetries(parser.nextIntValue(0));
            case ResponseFields.STATS_STORAGE_BYTES_READ:
                return builder.storageBytesRead(parser.nextIntValue(0));
            case ResponseFields.STATS_STORAGE_BYTES_WRITE:
                return builder.storageBytesWrite(parser.nextIntValue(0));
            case ResponseFields.STATS_RATE_LIMITS_HIT:
                List<String> limits = new ArrayList<>();
                if (parser.nextToken() == START_ARRAY) {
                    while (parser.nextToken() == VALUE_STRING) {
                        limits.add(parser.getValueAsString());
                    }
                }
                return builder.rateLimitsHit(limits);
            default:
                throw new ClientResponseException("Unknown field " + fieldName);
        }
    }


    public static QueryStats parseStats(JsonParser parser) throws IOException {
        if (parser.nextToken() == START_OBJECT) {
            Builder builder = builder();
            while (parser.nextToken() == FIELD_NAME) {
                builder = parseField(builder, parser);
            }
            return builder.build();
        } else if (parser.nextToken() == VALUE_NULL) {
            return null;
        } else {
            throw new ClientResponseException("Query stats should be an object or null, not " + parser.getCurrentToken());
        }
    }

    private static String statString(String name, Object value) {
        return String.join(": ", name, String.valueOf(value));

    }

    @Override
    public String toString() {
        if (this.stringValue == null) {
            this.stringValue = String.join(", ",
                    statString("compute", computeOps),
                    statString("read", readOps),
                    statString("write", writeOps),
                    statString("queryTime", queryTimeMs),
                    statString("retries", contentionRetries),
                    statString("storageRead", storageBytesRead),
                    statString("storageWrite", storageBytesWrite),
                    statString("limits", rateLimitsHit)
            );
        }
        return this.stringValue;
    }
}
