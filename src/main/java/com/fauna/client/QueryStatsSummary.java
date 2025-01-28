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
     * @param readOps                      Aggregate <a href="https://docs.fauna.com/fauna/current/manage/plans-billing/plan-details/#tro">Transactional
     *                                     Read Operations (TROs)</a> consumed
     *                                     by the requests.
     * @param computeOps                   Aggregate <a href="https://docs.fauna.com/fauna/current/manage/plans-billing/plan-details/#tco">Transactional
     *                                     Compute Operations (TCOs)</a>
     *                                     consumed by the requests.
     * @param writeOps                     Aggregate <a href="https://docs.fauna.com/fauna/current/manage/plans-billing/plan-details/#two">Transactional
     *                                     Write Operations (TWOs)</a>
     *                                     consumed by the requests.
     * @param queryTimeMs                  Aggregate query run time for the
     *                                     requests in milliseconds.
     * @param contentionRetries            Aggregate number of
     *                                     <a href="https://docs.fauna.com/fauna/current/learn/transactions/contention/#retries">retries
     *                                     for contended transactions</a>.
     * @param storageBytesRead             Aggregate amount of data read from
     *                                     storage, in bytes.
     * @param storageBytesWrite            Aggregate amount of data written to
     *                                     storage, in bytes.
     * @param processingTimeMs             Aggregate event processing time in
     *                                     milliseconds. Only applies to event
     *                                     feed and event stream requests.
     * @param queryCount                   Number of requests included in the
     *                                     summary.
     * @param rateLimitedReadQueryCount    Aggregate count of requests that
     *                                     exceeded
     *                                     <a href="https://docs.fauna.com/fauna/current/manage/plans-billing/plan-details/#throughput-limits">plan
     *                                     throughput limits</a> for
     *                                     <a href="https://docs.fauna.com/fauna/current/manage/plans-billing/plan-details/#tro">Transactional
     *                                     Read Operations (TROs)</a>.
     * @param rateLimitedComputeQueryCount Aggregate count of requests that
     *                                     exceeded
     *                                     <a href="https://docs.fauna.com/fauna/current/manage/plans-billing/plan-details/#throughput-limits">plan
     *                                     throughput limits</a> for
     *                                     <a href="https://docs.fauna.com/fauna/current/manage/plans-billing/plan-details/#tro">Transactional
     *                                     Compute Operations (TCOs)</a>.
     * @param rateLimitedWriteQueryCount   Aggregate count of requests that
     *                                     exceeded
     *                                     <a href="https://docs.fauna.com/fauna/current/manage/plans-billing/plan-details/#throughput-limits">plan
     *                                     throughput limits</a> for
     *                                     <a href="https://docs.fauna.com/fauna/current/manage/plans-billing/plan-details/#tro">Transactional
     *                                     Write Operations (TWOs)</a>.
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
     * Gets the aggregate <a href="https://docs.fauna.com/fauna/current/manage/plans-billing/plan-details/#tro">Transactional Read Operations (TROs)</a> recorded.
     *
     * @return A long representing the aggregate read ops
     */
    public long getReadOps() {
        return readOps;
    }

    /**
     * Gets the aggregate <a href="https://docs.fauna.com/fauna/current/manage/plans-billing/plan-details/#tco">Transactional Compute Operations (TCOs)</a> recorded.
     *
     * @return A long representing the aggregate compute ops
     */
    public long getComputeOps() {
        return computeOps;
    }

    /**
     * Gets the aggregate <a href="https://docs.fauna.com/fauna/current/manage/plans-billing/plan-details/#two">Transactional Write Operations (TWOs)</a>) recorded.
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
     * Applies to event feeds and event stream requests only.
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
