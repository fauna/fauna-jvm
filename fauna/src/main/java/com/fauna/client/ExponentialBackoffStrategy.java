package com.fauna.client;

import com.fauna.exception.FaunaException;

public class ExponentialBackoffStrategy implements RetryStrategy {
    private final float exponent;
    private final int maxAttempts;
    private final int initialIntervalMillis;
    private final int maxElapsedTimeMillis;
    private final float jitterFactor;

    ExponentialBackoffStrategy(float exponent, int maxAttepts, int initialIntervalMillis,  int maxElapsedTimeMillis,
                               float jitterFactor) {
        this.exponent = exponent;
        this.maxAttempts = maxAttepts;
        this.initialIntervalMillis = initialIntervalMillis;
        this.maxElapsedTimeMillis = maxElapsedTimeMillis;
        this.jitterFactor = jitterFactor;
    }

    private double getDeterministicDelay(int requestCount) {
        return Math.pow(this.exponent * this.initialIntervalMillis, requestCount);

    }

    private double getJitterPercent() {
        return Math.random() * jitterFactor;
    }

    @Override
    public int getDelayMillis(long initialRequestMillis, int requestCount) {
        if (initialRequestMillis + this.maxElapsedTimeMillis < System.currentTimeMillis()) {
            throw new FaunaException("Exceeded maxElapsedTimeMillis.");
        } else if (requestCount >= maxAttempts) {
            throw new FaunaException("Exceeded maxAttempts");
        } else {
            double delay = getDeterministicDelay(requestCount);
            return (int) (delay + delay * getJitterPercent());
        }
    }

    public static class Builder {
        public float exponent = 2.0f;
        public int maxAttempts = 3;
        public int initialIntervalMillis = 1000;
        public int maxElapsedTimeMillis = 10_000;
        public float jitterFactor = 0.25f;

        public Builder exponent(float exponent) {
            this.exponent = exponent;
            return this;
        }

        public Builder maxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        public Builder initialIntervalMillis(int initialIntervalMillis) {
            this.initialIntervalMillis = initialIntervalMillis;
            return this;
        }

        public Builder maxElapsedTimeMillis(int maxElapsedTimeMillis) {
            this.maxElapsedTimeMillis = maxElapsedTimeMillis;
            return this;
        }

        public Builder jitterFactor(float jitterFactor) {
            this.jitterFactor = jitterFactor;
            return this;
        }

        public ExponentialBackoffStrategy build() {
            return new ExponentialBackoffStrategy(
                    this.exponent, this.maxAttempts, this.initialIntervalMillis,
                    this.maxElapsedTimeMillis, this.jitterFactor);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}
