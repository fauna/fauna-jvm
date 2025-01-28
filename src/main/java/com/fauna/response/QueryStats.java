package com.fauna.response;

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

    private final int computeOps;
    private final int readOps;
    private final int writeOps;
    private final int queryTimeMs;
    private final int processingTimeMs;
    private final int contentionRetries;
    private final int storageBytesRead;
    private final int storageBytesWrite;
    private final List<String> rateLimitsHit;

    private String stringValue = null;

    /**
     * @param computeOps        <a href="https://docs.fauna.com/fauna/current/manage/plans-billing/plan-details/#tco">Transactional
     *                          Compute Operations (TCOs)</a> consumed by the request.
     * @param readOps           <a href="https://docs.fauna.com/fauna/current/manage/plans-billing/plan-details/#tro">Transactional
     *                          Read Operations (TROs)</a> consumed by the request.
     * @param writeOps          <a href="https://docs.fauna.com/fauna/current/manage/plans-billing/plan-details/#two">Transactional
     *                          Write Operations (TROs)</a> consumed by the request.
     * @param queryTimeMs       Query run time for the request in milliseconds.
     * @param contentionRetries Number of <a href="https://docs.fauna.com/fauna/current/learn/transactions/contention/#retries">retries
     *                          for contended transactions</a>
     * @param storageBytesRead  Amount of data read from storage, in bytes.
     * @param storageBytesWrite Amount of data written to storage, in bytes.
     * @param processingTimeMs  Aggregate event processing time in milliseconds.
     *                          Only applies to event feed and event stream
     *                          requests.
     * @param rateLimitsHit     Operation types that exceeded
     *                          <a href="https://docs.fauna.com/fauna/current/manage/plans-billing/plan-details/#throughput-limits">plan
     *                          throughput limits</a>.
     */
    public QueryStats(final int computeOps, final int readOps,
                      final int writeOps,
                      final int queryTimeMs, final int contentionRetries,
                      final int storageBytesRead, final int storageBytesWrite,
                      final int processingTimeMs,
                      final List<String> rateLimitsHit) {
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

    static Builder builder() {
        return new Builder();
    }

    static Builder parseField(final Builder builder, final JsonParser parser)
            throws IOException {
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

    /**
     * Parse QueryStats from a JsonParser.
     *
     * @param parser the JsonParser to consume
     * @return a QueryStats object containing the parsed stats
     * @throws IOException thrown from the JsonParser
     */
    public static QueryStats parseStats(final JsonParser parser)
            throws IOException {
        if (parser.nextToken() == START_OBJECT) {
            Builder builder = builder();
            while (parser.nextToken() == FIELD_NAME) {
                builder = parseField(builder, parser);
            }
            return builder.build();
        } else if (parser.nextToken() == VALUE_NULL) {
            return null;
        } else {
            throw new ClientResponseException(
                    "Query stats should be an object or null, not "
                            + parser.getCurrentToken());
        }
    }

    private static String statString(final String name, final Object value) {
        return String.join(": ", name, String.valueOf(value));
    }

    /**
     * Gets the Transactional Compute Operations (TCOs) recorded.
     *
     * @return An int representing the compute ops.
     */
    public int getComputeOps() {
        return computeOps;
    }

    /**
     * Gets the <a href="https://docs.fauna.com/fauna/current/manage/plans-billing/plan-details/#tro">Transactional Read Operations (TROs)</a> recorded.
     *
     * @return An int representing the read ops.
     */
    public int getReadOps() {
        return readOps;
    }

    /**
     * Gets the <a href="https://docs.fauna.com/fauna/current/manage/plans-billing/plan-details/#two">Transactional Write Operations (TWOs)</a> recorded.
     *
     * @return An int representing the write ops.
     */
    public int getWriteOps() {
        return writeOps;
    }

    /**
     * Gets the query time in milliseconds.
     *
     * @return An int representing the query time in milliseconds.
     */
    public int getQueryTimeMs() {
        return queryTimeMs;
    }

    /**
     * Gets the event processing time in milliseconds.
     * Applies to event feeds and event stream requests only.
     *
     * @return An int representing the processing time in milliseconds.
     */
    public int getProcessingTimeMs() {
        return processingTimeMs;
    }

    /**
     * Gets the number of <a href="https://docs.fauna.com/fauna/current/learn/transactions/contention/#retries">retries
     * for transaction contention</a>.
     *
     * @return An int representing the number of transaction contention retries.
     */
    public int getContentionRetries() {
        return contentionRetries;
    }

    /**
     * Gets the amount of data read from storage in bytes.
     *
     * @return An int representing the number of bytes read.
     */
    public int getStorageBytesRead() {
        return storageBytesRead;
    }

    /**
     * Gets the amount of data written to storage in bytes.
     *
     * @return An int representing the number of bytes written.
     */
    public int getStorageBytesWrite() {
        return storageBytesWrite;
    }

    /**
     * Gets a list of operation types that exceeded their <a href="https://docs.fauna.com/fauna/current/manage/plans-billing/plan-details/#throughput-limits">plan
     * throughput limits</a>.
     *
     * @return A list of operation types that exceeded their throughput limit.
     */
    public List<String> getRateLimitsHit() {
        return rateLimitsHit;
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

    static class Builder {
        private int computeOps;
        private int readOps;
        private int writeOps;
        private int queryTimeMs;
        private int contentionRetries;
        private int storageBytesRead;
        private int storageBytesWrite;
        private int processingTimeMs;
        private List<String> rateLimitsHit;

        Builder computeOps(final int value) {
            this.computeOps = value;
            return this;
        }

        Builder readOps(final int value) {
            this.readOps = value;
            return this;
        }

        Builder writeOps(final int value) {
            this.writeOps = value;
            return this;
        }

        Builder queryTimeMs(final int value) {
            this.queryTimeMs = value;
            return this;
        }

        Builder contentionRetries(final int value) {
            this.contentionRetries = value;
            return this;
        }

        Builder storageBytesRead(final int value) {
            this.storageBytesRead = value;
            return this;
        }

        Builder storageBytesWrite(final int value) {
            this.storageBytesWrite = value;
            return this;
        }

        Builder processingTimeMs(final int value) {
            this.processingTimeMs = value;
            return this;
        }

        Builder rateLimitsHit(final List<String> value) {
            this.rateLimitsHit = value;
            return this;
        }

        QueryStats build() {
            return new QueryStats(computeOps, readOps, writeOps, queryTimeMs,
                    contentionRetries, storageBytesRead, storageBytesWrite,
                    processingTimeMs, rateLimitsHit);
        }
    }
}
