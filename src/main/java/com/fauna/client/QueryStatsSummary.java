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

    /**
     * @param readOps                      the read ops
     * @param computeOps                   the compute ops
     * @param writeOps                     the write ops
     * @param queryTimeMs                  the query time in milliseconds
     * @param contentionRetries            the number of retries due to
     *                                     contention
     * @param storageBytesRead             the number of storage bytes read
     * @param storageBytesWrite            the number of storage bytes written
     * @param processingTimeMs             the event processing time in
     *                                     milliseconds
     * @param queryCount                   the number of queries included in the
     *                                     summary
     * @param rateLimitedReadQueryCount    the count of queries limited
     *                                     by read ops
     * @param rateLimitedComputeQueryCount the count of queries limited
     *                                     by compute ops
     * @param rateLimitedWriteQueryCount   the count of queries limited
     *                                     by write ops
     */
    public QueryStatsSummary(
            final long readOps,
            final long computeOps,
            final long writeOps,
            final long queryTimeMs,
            final int contentionRetries,
            final long storageBytesRead,
            final long storageBytesWrite,
            final long processingTimeMs,
            final int queryCount,
            final int rateLimitedReadQueryCount,
            final int rateLimitedComputeQueryCount,
            final int rateLimitedWriteQueryCount
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
     * Gets the aggregate Transactional Read Operations (TROs) recorded.
     *
     * @return A long representing the aggregate read ops
     */
    public long getReadOps() {
        return readOps;
    }

    /**
     * Gets the aggregate Transactional Compute Operations (TCOs) recorded.
     *
     * @return A long representing the aggregate compute ops
     */
    public long getComputeOps() {
        return computeOps;
    }

    /**
     * Gets the aggregate Transactional Write Operations (TWOs) recorded.
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
     * @return A long representing the aggregate number of storage bytes
     * written.
     */
    public long getStorageBytesWrite() {
        return storageBytesWrite;
    }

    /**
     * Gets the aggregate event processing time in milliseconds.
     * Applies to Streams and Feeds only.
     *
     * @return A long representing the aggregate processing time in
     * milliseconds.
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
