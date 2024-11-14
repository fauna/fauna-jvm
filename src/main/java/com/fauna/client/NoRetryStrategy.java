package com.fauna.client;

/**
 * Specifies that no retries will be made.
 */
public final class NoRetryStrategy implements RetryStrategy {

    @Override
    public boolean canRetry(final int retryAttempt) {
        return false;
    }

    @Override
    public int getDelayMillis(final int retryAttempt) {
        return 0;
    }

    @Override
    public int getMaxRetryAttempts() {
        return 0;
    }
}
