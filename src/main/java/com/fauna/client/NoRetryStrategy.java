package com.fauna.client;

public class NoRetryStrategy implements RetryStrategy {


    @Override
    public boolean canRetry(int retryAttempt) {
        return false;
    }

    @Override
    public int getDelayMillis(int retryAttempt) {
        return 0;
    }

    @Override
    public int getMaxRetryAttempts() {
        return 0;
    }
}
