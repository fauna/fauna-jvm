package com.fauna.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fauna.constants.ResponseFields;
import com.fauna.exception.ClientException;
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
        this.processingTimeMs = queryTimeMs;
        this.contentionRetries = contentionRetries;
        this.storageBytesRead = storageBytesRead;
        this.storageBytesWrite = storageBytesWrite;
        this.rateLimitsHit = rateLimitsHit != null ? rateLimitsHit : List.of();
    }


    public static QueryStats parseStats(JsonParser parser) throws IOException {
        if (parser.nextToken() == START_OBJECT) {
            int computeOps = 0;
            int readOps = 0;
            int writeOps = 0;
            int queryTimeMs = 0;
            int processingTimeMs = 0;
            int contentionRetries = 0;
            int storageBytesRead = 0;
            int storageBytesWrite = 0;
            List<String> rateLimitsHit = null;
            while (parser.nextToken() == FIELD_NAME) {
                String fieldName = parser.getValueAsString();
                switch (fieldName) {
                    case ResponseFields.STATS_COMPUTE_OPS_FIELD_NAME:
                        computeOps = parser.nextIntValue(0);
                        break;
                    case ResponseFields.STATS_READ_OPS:
                        readOps = parser.nextIntValue(0);
                        break;
                    case ResponseFields.STATS_WRITE_OPS:
                        writeOps = parser.nextIntValue(0);
                        break;
                    case ResponseFields.STATS_QUERY_TIME_MS:
                        queryTimeMs = parser.nextIntValue(0);
                        break;
                    case ResponseFields.STATS_PROCESSING_TIME_MS:
                        processingTimeMs = parser.nextIntValue(0);
                        break;
                    case ResponseFields.STATS_CONTENTION_RETRIES:
                        contentionRetries = parser.nextIntValue(0);
                        break;
                    case ResponseFields.STATS_STORAGE_BYTES_READ:
                        storageBytesRead = parser.nextIntValue(0);
                        break;
                    case ResponseFields.STATS_STORAGE_BYTES_WRITE:
                        storageBytesWrite = parser.nextIntValue(0);
                        break;
                    case ResponseFields.STATS_RATE_LIMITS_HIT:
                        List<String> limits = new ArrayList<>();
                        if (parser.nextToken() == START_ARRAY) {
                            while (parser.nextToken() == VALUE_STRING) {
                                limits.add(parser.getValueAsString());
                            }
                        }
                        rateLimitsHit = limits;
                        break;
                    default:
                        throw new ClientResponseException("Unknown field " + fieldName);
                }
            }
            return new QueryStats(computeOps, readOps, writeOps, queryTimeMs, processingTimeMs, contentionRetries, storageBytesRead, storageBytesWrite, rateLimitsHit);
        } else if (parser.nextToken() == VALUE_NULL) {
            return null;
        } else {
            throw new ClientResponseException("Query stats should be an object or null, not " + parser.getCurrentToken());
        }
    }

    @Override
    public String toString() {
        return "compute: " + computeOps + ", read: " + readOps + ", write: " + writeOps + ", " +
            "queryTime: " + queryTimeMs + ", retries: " + contentionRetries + ", " +
            "storageRead: " + storageBytesRead + ", storageWrite: " + storageBytesWrite + ", " +
            "limits: " + rateLimitsHit.toString();
    }
}
