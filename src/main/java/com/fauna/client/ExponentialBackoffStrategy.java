package com.fauna.client;

/**
 * Implements an exponential backoff strategy for retries.
 * The backoff delay increases exponentially with each retry attempt, with optional jitter.
 */
public final class ExponentialBackoffStrategy implements RetryStrategy {
    private final float backoffFactor;
    private final int maxAttempts;
    private final int initialIntervalMillis;
    private final int maxBackoffMillis;
    private final float jitterFactor;

    /**
     * Constructs an Exponential backoff strategy.
     *
     * @param maxAttempts           The maximum number of retry attempts. Defaults to 3 retries.
     * @param backoffFactor         The factor by which the delay will increase. Default is 2.
     * @param initialIntervalMillis The interval (in milliseconds) for the first retry attempt. Default is 1000ms.
     * @param maxBackoffMillis      The maximum delay (in milliseconds) between retries. Default is 20000ms.
     * @param jitterFactor          A value between 0 and 1 that controls the jitter factor. Default is 0.5.
     */
    ExponentialBackoffStrategy(final int maxAttempts, final float backoffFactor,
                               final int initialIntervalMillis,
                               final int maxBackoffMillis, final float jitterFactor) {
        this.maxAttempts = maxAttempts;
        this.backoffFactor = backoffFactor;
        this.initialIntervalMillis = initialIntervalMillis;
        this.maxBackoffMillis = maxBackoffMillis;
        this.jitterFactor = jitterFactor;

        if (jitterFactor < 0.0 || jitterFactor > 1.0) {
            throw new IllegalArgumentException("Jitter factor must be between 0 and 1.");
        }
        if (backoffFactor < 0.0) {
            throw new IllegalArgumentException("Backoff factor must be positive.");
        }
        if (maxAttempts < 0) {
            throw new IllegalArgumentException("Max attempts must be a natural number (not negative).");
        }
        if (initialIntervalMillis < 0) {
            throw new IllegalArgumentException("Initial interval must be positive.");
        }
        if (maxBackoffMillis < 0) {
            throw new IllegalArgumentException("Max backoff must be positive.");
        }
    }

    /**
     * Generates a random jitter percent between 0 and the jitterFactor.
     *
     * @return A random jitter percent.
     */
    private double getJitterPercent() {
        return Math.random() * jitterFactor;
    }

    @Override
    public boolean canRetry(final int retryAttempt) {
        if (retryAttempt < 0) {
            throw new IllegalArgumentException("Retry attempt must be a natural number (not negative).");
        }
        return retryAttempt <= maxAttempts;
    }

    @Override
    public int getDelayMillis(final int retryAttempt) {
        if (retryAttempt < 0) {
            throw new IllegalArgumentException("Retry attempt must be a natural number (not negative).");
        } else if (retryAttempt == 0) {
            return 0;
        } else {
            double deterministicBackoff = Math.pow(this.backoffFactor, retryAttempt - 1);
            double calculatedBackoff = deterministicBackoff * (1 - getJitterPercent()) * initialIntervalMillis;
            return (int) Math.min(calculatedBackoff, this.maxBackoffMillis);
        }
    }

    @Override
    public int getMaxRetryAttempts() {
        return this.maxAttempts;
    }

    /**
     * Builder class for the ExponentialBackoffStrategy.
     * Allows fluent configuration of the backoff strategy parameters.
     */
    public static class Builder {
        private float backoffFactor = 2.0f;
        private int maxAttempts = 3;
        private int initialIntervalMillis = 1000;
        private int maxBackoffMillis = 20_000;
        private float jitterFactor = 0.5f;

        /**
         * Sets the maximum number of retry attempts.
         *
         * @param maxAttempts The maximum number of retry attempts.
         * @return The current Builder instance.
         */
        public Builder maxAttempts(final int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        /**
         * Sets the backoff factor.
         *
         * @param backoffFactor The factor by which the backoff delay increases.
         * @return The current Builder instance.
         */
        public Builder backoffFactor(final float backoffFactor) {
            this.backoffFactor = backoffFactor;
            return this;
        }

        /**
         * Sets the initial interval (in milliseconds) for the first retry attempt.
         *
         * @param initialIntervalMillis The initial interval in milliseconds.
         * @return The current Builder instance.
         */
        public Builder initialIntervalMillis(final int initialIntervalMillis) {
            this.initialIntervalMillis = initialIntervalMillis;
            return this;
        }

        /**
         * Sets the maximum backoff (in milliseconds) between retries.
         *
         * @param maxBackoffMillis The maximum backoff in milliseconds.
         * @return The current Builder instance.
         */
        public Builder maxBackoffMillis(final int maxBackoffMillis) {
            this.maxBackoffMillis = maxBackoffMillis;
            return this;
        }

        /**
         * Sets the jitter factor (between 0 and 1) to control how much to jitter the backoff delay.
         *
         * @param jitterFactor The jitter factor.
         * @return The current Builder instance.
         */
        public Builder jitterFactor(final float jitterFactor) {
            this.jitterFactor = jitterFactor;
            return this;
        }

        /**
         * Builds and returns a new ExponentialBackoffStrategy instance.
         *
         * @return A new ExponentialBackoffStrategy.
         */
        public ExponentialBackoffStrategy build() {
            return new ExponentialBackoffStrategy(
                    this.maxAttempts, this.backoffFactor,
                    this.initialIntervalMillis,
                    this.maxBackoffMillis, this.jitterFactor);
        }
    }

    /**
     * Creates a new Builder instance for ExponentialBackoffStrategy.
     *
     * @return A new Builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

}
