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
     * Return the number of milliseconds to delay the next retry attempt.
     * @param retryAttempt  The retry attempt number, starting at 1 (i.e. the second overall attempt/first retry is #1).
     * @return              The number of milliseconds to delay the next retry attempt.
     */
    int getDelayMillis(int retryAttempt);

    /**
     * Return the maximum number of retry attempts for this strategy.
     * @return  The number of retry attempts that this strategy will attempt.
     */
    int getMaxRetryAttempts();


}
