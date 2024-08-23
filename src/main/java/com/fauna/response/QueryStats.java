package com.fauna.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public final class QueryStats {

    @JsonProperty(ResponseFields.STATS_COMPUTE_OPS_FIELD_NAME)
    public final int computeOps;

    @JsonProperty(ResponseFields.STATS_READ_OPS)
    public final int readOps;

    @JsonProperty(ResponseFields.STATS_WRITE_OPS)
    public final int writeOps;

    @JsonProperty(ResponseFields.STATS_QUERY_TIME_MS)
    public final int queryTimeMs;

    @JsonProperty(ResponseFields.STATS_CONTENTION_RETRIES)
    public final int contentionRetries;

    @JsonProperty(ResponseFields.STATS_STORAGE_BYTES_READ)
    public final int storageBytesRead;

    @JsonProperty(ResponseFields.STATS_STORAGE_BYTES_WRITE)
    public final int storageBytesWrite;

    @JsonProperty(ResponseFields.STATS_RATE_LIMITS_HIT)
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
        this.rateLimitsHit = rateLimitsHit;
    }

    @Override
    public String toString() {
        return "compute: " + computeOps + ", read: " + readOps + ", write: " + writeOps + ", " +
            "queryTime: " + queryTimeMs + ", retries: " + contentionRetries + ", " +
            "storageRead: " + storageBytesRead + ", storageWrite: " + storageBytesWrite + ", " +
            "limits: " + rateLimitsHit.toString();
    }
}
