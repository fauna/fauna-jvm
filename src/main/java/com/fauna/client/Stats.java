package com.fauna.client;

public final class Stats {
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

    public Stats(
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

    public long getReadOps() { return readOps; }
    public long getComputeOps() { return computeOps; }
    public long getWriteOps() { return writeOps; }
    public long getQueryTimeMs() { return queryTimeMs; }
    public int getContentionRetries() { return contentionRetries; }
    public long getStorageBytesRead() { return storageBytesRead; }
    public long getStorageBytesWrite() { return storageBytesWrite; }
    public long getProcessingTimeMs() { return processingTimeMs; }
    public int getQueryCount() { return queryCount; }

    public int getRateLimitedReadQueryCount() { return rateLimitedReadQueryCount; }
    public int getRateLimitedComputeQueryCount() { return rateLimitedComputeQueryCount; }
    public int getRateLimitedWriteQueryCount() { return rateLimitedWriteQueryCount; }
}
