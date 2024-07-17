package com.fauna.client;

public class RetryHandler {
    private final RetryStrategy strategy;
    private final long startTimeMillis;
    private int requestCount = 0;


    public RetryHandler(RetryStrategy strategy) {
        this.strategy = strategy;
        this.startTimeMillis = System.currentTimeMillis();
    }

    public int getDelayMillis() {
        this.requestCount += 1;
        return this.strategy.getDelayMillis(startTimeMillis, requestCount);
    }
}
