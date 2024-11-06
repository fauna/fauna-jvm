package com.fauna.client;

/**
 * A class for representing aggregate query stats. This should be used when collecting query stats
 * across multiple requests.
 * <p>
 * For a single request, use @link com.fauna.response.QueryStats instead.
 */
public final class QueryStatsSummary {
    private final long readOps;
    private final long computeOps;
    private final long writeOps;
    private final long queryTimeMs;
    private final int contentionRetries;
    private final long storageBytesRead;
    private final long storageBytesWrite;
    private final long processingTimeMs;
    private final int queryCount;

    private final int rateLimitedReadQueryCount;
    private final int rateLimitedComputeQueryCount;
    private final int rateLimitedWriteQueryCount;

    public QueryStatsSummary(
            long readOps,
            long computeOps,
            long writeOps,
            long queryTimeMs,
            int contentionRetries,
            long storageBytesRead,
            long storageBytesWrite,
            long processingTimeMs,
            int queryCount,
            int rateLimitedReadQueryCount,
            int rateLimitedComputeQueryCount,
            int rateLimitedWriteQueryCount
    ) {
        this.readOps = readOps;
        this.computeOps = computeOps;
        this.writeOps = writeOps;
        this.queryTimeMs = queryTimeMs;
        this.contentionRetries = contentionRetries;
        this.storageBytesRead = storageBytesRead;
        this.storageBytesWrite = storageBytesWrite;
        this.processingTimeMs = processingTimeMs;
        this.queryCount = queryCount;
        this.rateLimitedReadQueryCount = rateLimitedReadQueryCount;
        this.rateLimitedComputeQueryCount = rateLimitedComputeQueryCount;
        this.rateLimitedWriteQueryCount = rateLimitedWriteQueryCount;
    }

    /**
     * Gets the aggregate read ops.
     *
     * @return A long representing the aggregate read ops
     */
    public long getReadOps() {
        return readOps;
    }

    /**
     * Gets the aggregate compute ops.
     *
     * @return A long representing the aggregate compute ops
     */
    public long getComputeOps() {
        return computeOps;
    }

    /**
     * Gets the aggregate write ops.
     *
     * @return A long representing the aggregate write ops
     */
    public long getWriteOps() {
        return writeOps;
    }

    /**
     * Gets the aggregate query time in milliseconds.
     *
     * @return A long representing the aggregate query time in milliseconds.
     */
    public long getQueryTimeMs() {
        return queryTimeMs;
    }

    /**
     * Gets the count of retries due to contention.
     *
     * @return An int representing the count of retries due to contention.
     */
    public int getContentionRetries() {
        return contentionRetries;
    }

    /**
     * Gets the aggregate storage bytes read.
     *
     * @return A long representing the aggregate number of storage bytes read.
     */
    public long getStorageBytesRead() {
        return storageBytesRead;
    }

    /**
     * Gets the aggregate storage bytes written.
     *
     * @return A long representing the aggregate number of storage bytes written.
     */
    public long getStorageBytesWrite() {
        return storageBytesWrite;
    }

    /**
     * Gets the aggregate processing time in milliseconds.
     *
     * @return A long representing the aggregate processing time in milliseconds.
     */
    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    /**
     * Gets the count of queries summarized on this instance.
     *
     * @return An int representing the count of queries summarized.
     */
    public int getQueryCount() {
        return queryCount;
    }

    /**
     * Gets the count of rate limited queries due to read limits.
     *
     * @return An int representing the count of rate limited queries.
     */
    public int getRateLimitedReadQueryCount() {
        return rateLimitedReadQueryCount;
    }

    /**
     * Gets the count of rate limited queries due to compute limits.
     *
     * @return An int representing the count of rate limited queries.
     */
    public int getRateLimitedComputeQueryCount() {
        return rateLimitedComputeQueryCount;
    }

    /**
     * Gets the count of rate limited queries due to write limits.
     *
     * @return An int representing the count of rate limited queries.
     */
    public int getRateLimitedWriteQueryCount() {
        return rateLimitedWriteQueryCount;
    }
}
