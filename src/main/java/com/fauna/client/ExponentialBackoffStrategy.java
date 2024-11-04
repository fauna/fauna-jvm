package com.fauna.client;


public class ExponentialBackoffStrategy implements RetryStrategy {
    private final float backoffFactor;
    private final int maxAttempts;
    private final int initialIntervalMillis;
    private final int maxBackoffMillis;
    private final float jitterFactor;

    /**
     * Construct an Exponential backoff strategy.
     * The basic formula for exponential backoff is `b^(a-1)` where `b` is the backoff factor, and `a` is the retry
     * attempt number. So for a backoff factor of 2, you get:
     * 2^0=1, 2^1=2, 2^3=4, 2^4=8 ...
     *
     * @param maxAttempts           The maximum amount of retry attempts. Defaults to 3 retry attempts which means
     *                              the client will make a total of 4 requests before giving up.
     * @param backoffFactor         Defines how quickly the client will back off, default is 2.
     *                              A value of 1 would not backoff (not recommended).
     * @param initialIntervalMillis Defines the interval for the first wait. Default is 1000ms.
     * @param maxBackoffMillis      Set a cap on the delay between requests. The default is 20,000ms
     * @param jitterFactor          A value between 0 (0%) and 1 (100%) that controls how much to jitter the delay.
     *                              The default is 0.5.
     */
    ExponentialBackoffStrategy(int maxAttempts, float backoffFactor,
                               int initialIntervalMillis,
                               int maxBackoffMillis, float jitterFactor) {
        this.maxAttempts = maxAttempts;
        this.backoffFactor = backoffFactor;
        this.initialIntervalMillis = initialIntervalMillis;
        this.maxBackoffMillis = maxBackoffMillis;
        this.jitterFactor = jitterFactor;
        if (jitterFactor < 0.0 || jitterFactor > 1.0) {
            throw new IllegalArgumentException(
                    "Jitter factor must be between 0 and 1.");
        }
        if (backoffFactor < 0.0) {
            throw new IllegalArgumentException(
                    "Backoff factor must be positive.");
        }
        if (maxAttempts < 0) {
            throw new IllegalArgumentException(
                    "Max attempts must be a natural number (not negative).");
        }
        if (initialIntervalMillis < 0) {
            throw new IllegalArgumentException(
                    "Initial interval must be positive.");
        }
        if (maxBackoffMillis < 0) {
            throw new IllegalArgumentException("Max backoff must be positive.");
        }
    }

    /**
     * Get the % to jitter the backoff, will be a value between 0 and jitterFactor.
     *
     * @return A randomly generated value between 0 and jitterFactor.
     */
    private double getJitterPercent() {
        return Math.random() * jitterFactor;
    }

    @Override
    public boolean canRetry(int retryAttempt) {
        if (retryAttempt < 0) {
            throw new IllegalArgumentException(
                    "Retry attempt must be a natural number (not negative).");
        }
        return retryAttempt <= maxAttempts;
    }

    @Override
    public int getDelayMillis(int retryAttempt) {
        if (retryAttempt < 0) {
            throw new IllegalArgumentException(
                    "Retry attempt must be a natural number (not negative).");
        } else if (retryAttempt == 0) {
            return 0;
        } else {
            double deterministicBackoff =
                    Math.pow(this.backoffFactor, retryAttempt - 1);
            double calculatedBackoff =
                    deterministicBackoff * (1 - getJitterPercent()) *
                            initialIntervalMillis;
            return (int) Math.min(calculatedBackoff, this.maxBackoffMillis);
        }
    }

    @Override
    public int getMaxRetryAttempts() {
        return this.maxAttempts;
    }


    /**
     * Build a new ExponentialBackoffStrategy. This builder only supports setting maxAttempts, because that's the only
     * variable that we recommend users change in production. If you need to modify other values for debugging, or other
     * purposes, then you can use the constructor directly.
     */
    public static class Builder {
        private float backoffFactor = 2.0f;
                // Results in delay of 1, 2, 4, 8, 16... seconds.
        private int maxAttempts = 3;     // Limits number of retry attempts.
        private int initialIntervalMillis = 1000;
        private int maxBackoffMillis = 20_000;
        // A jitterFactor of 0.5, combined with a backoffFactor of 2 ensures that the delay is always increasing.
        private float jitterFactor = 0.5f;


        public Builder maxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        public Builder backoffFactor(float backoffFactor) {
            this.backoffFactor = backoffFactor;
            return this;
        }

        public Builder initialIntervalMillis(int initialIntervalMillis) {
            this.initialIntervalMillis = initialIntervalMillis;
            return this;
        }

        public Builder maxBackoffMillis(int maxBackoffMillis) {
            this.maxBackoffMillis = maxBackoffMillis;
            return this;
        }

        public Builder jitterFactor(float jitterFactor) {
            this.jitterFactor = jitterFactor;
            return this;
        }

        public ExponentialBackoffStrategy build() {
            return new ExponentialBackoffStrategy(
                    this.maxAttempts, this.backoffFactor,
                    this.initialIntervalMillis,
                    this.maxBackoffMillis, this.jitterFactor);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}
