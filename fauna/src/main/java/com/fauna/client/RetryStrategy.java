package com.fauna.client;

/**
 * This client comes with an ExponentialRetryStrategy, and it is recommended that users stick with that. If you choose
 * to implement your own RetryStrategy, then it should implement this interface, and be thread-safe
 * (or not store state).
 */
public interface RetryStrategy {

    /**
     * Returns true if the given retry attempt will be allowed by this strategy.
     * @param retryAttempt  The retry attempt number, starting at 1 (i.e. the second overall attempt, or first retry is attempt 1).
     * @return              True if this attempt can be retried, otherwise false.
     */
    boolean canRetry(int retryAttempt);

    /**
     * Return the number of milliseconds to delay the next attempt.
     * @param retryAttempt  The retry attempt number, starting at 1 (i.e. the second overall attempt, or first retry is attempt 1).
     * @return
     */
    int getDelayMillis(int retryAttempt);


}
