package com.fauna.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fauna.constants.ResponseFields;

import java.util.List;

public final class QueryStats {

    public final int computeOps;

    public final int readOps;

    public final int writeOps;

    public final int queryTimeMs;

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
            @JsonProperty(ResponseFields.STATS_RATE_LIMITS_HIT) List<String> rateLimitsHit) {
        this.computeOps = computeOps;
        this.readOps = readOps;
        this.writeOps = writeOps;
        this.queryTimeMs = queryTimeMs;
        this.contentionRetries = contentionRetries;
        this.storageBytesRead = storageBytesRead;
        this.storageBytesWrite = storageBytesWrite;
        this.rateLimitsHit = rateLimitsHit != null ? rateLimitsHit : List.of();
    }

    @Override
    public String toString() {
        return "compute: " + computeOps + ", read: " + readOps + ", write: " + writeOps + ", " +
            "queryTime: " + queryTimeMs + ", retries: " + contentionRetries + ", " +
            "storageRead: " + storageBytesRead + ", storageWrite: " + storageBytesWrite + ", " +
            "limits: " + rateLimitsHit.toString();
    }
}
