package com.fauna.perf;

import java.time.Instant;

public class TestTimings {
    private final Instant createdAt;
    private final int roundTripMs;
    private final int queryTimeMs;
    private final int overheadMs;

    public TestTimings(int roundTripMs, int queryTimeMs, int overheadMs) {
        this.createdAt = Instant.now();
        this.roundTripMs = roundTripMs;
        this.queryTimeMs = queryTimeMs;
        this.overheadMs = overheadMs;
    }

    // Getters
    public Instant getCreatedAt() {
        return createdAt;
    }

    public int getRoundTripMs() {
        return roundTripMs;
    }

    public int getQueryTimeMs() {
        return queryTimeMs;
    }

    public int getOverheadMs() {
        return overheadMs;
    }
}
