package com.fauna.client;

import com.fauna.response.QueryStats;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class StatsCollectorImpl implements StatsCollector {

    private static final String RATE_LIMIT_READ_OPS = "read";
    private static final String RATE_LIMIT_COMPUTE_OPS = "compute";
    private static final String RATE_LIMIT_WRITE_OPS = "write";

    private final AtomicLong readOps = new AtomicLong();
    private final AtomicLong computeOps = new AtomicLong();
    private final AtomicLong writeOps = new AtomicLong();
    private final AtomicLong queryTimeMs = new AtomicLong();
    private final AtomicInteger contentionRetries = new AtomicInteger();
    private final AtomicLong storageBytesRead = new AtomicLong();
    private final AtomicLong storageBytesWrite = new AtomicLong();
    private final AtomicLong processingTimeMs = new AtomicLong();
    private final AtomicInteger queryCount = new AtomicInteger();
    private final AtomicInteger rateLimitedReadQueryCount = new AtomicInteger();
    private final AtomicInteger rateLimitedComputeQueryCount =
            new AtomicInteger();
    private final AtomicInteger rateLimitedWriteQueryCount =
            new AtomicInteger();

    @Override
    public void add(QueryStats stats) {
        readOps.addAndGet(stats.getReadOps());
        computeOps.addAndGet(stats.getComputeOps());
        writeOps.addAndGet(stats.getWriteOps());
        queryTimeMs.addAndGet(stats.getQueryTimeMs());
        contentionRetries.addAndGet(stats.getContentionRetries());
        storageBytesRead.addAndGet(stats.getStorageBytesRead());
        storageBytesWrite.addAndGet(stats.getStorageBytesWrite());
        processingTimeMs.addAndGet(stats.getProcessingTimeMs());

        List<String> rateLimitsHit = stats.getRateLimitsHit();
        rateLimitsHit.forEach(limitHit -> {
            switch (limitHit) {
                case RATE_LIMIT_READ_OPS:
                    rateLimitedReadQueryCount.incrementAndGet();
                    break;
                case RATE_LIMIT_COMPUTE_OPS:
                    rateLimitedComputeQueryCount.incrementAndGet();
                    break;
                case RATE_LIMIT_WRITE_OPS:
                    rateLimitedWriteQueryCount.incrementAndGet();
                    break;
            }
        });

        queryCount.incrementAndGet();
    }

    @Override
    public QueryStatsSummary read() {
        return new QueryStatsSummary(
                readOps.get(),
                computeOps.get(),
                writeOps.get(),
                queryTimeMs.get(),
                contentionRetries.get(),
                storageBytesRead.get(),
                storageBytesWrite.get(),
                processingTimeMs.get(),
                queryCount.get(),
                rateLimitedReadQueryCount.get(),
                rateLimitedComputeQueryCount.get(),
                rateLimitedWriteQueryCount.get()
        );
    }

    @Override
    public QueryStatsSummary readAndReset() {
        return new QueryStatsSummary(
                readOps.getAndSet(0),
                computeOps.getAndSet(0),
                writeOps.getAndSet(0),
                queryTimeMs.getAndSet(0),
                contentionRetries.getAndSet(0),
                storageBytesRead.getAndSet(0),
                storageBytesWrite.getAndSet(0),
                processingTimeMs.getAndSet(0),
                queryCount.getAndSet(0),
                rateLimitedReadQueryCount.getAndSet(0),
                rateLimitedComputeQueryCount.getAndSet(0),
                rateLimitedWriteQueryCount.getAndSet(0)
        );
    }

    @Override
    public StatsCollector clone() {
        return new StatsCollectorImpl();
    }
}

