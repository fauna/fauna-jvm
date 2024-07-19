package com.fauna.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestExponentialBackoffStrategy {

    @Test
    public void testNoRetriesBehavior() {
        RetryStrategy strategy = ExponentialBackoffStrategy.NO_RETRIES;

        assertTrue(strategy.canRetry(0));
        assertFalse(strategy.canRetry(1));
    }

    @Test
    public void testBackoffBehaviour() {
        // Set jitter to 0 just to make testing easier.
        RetryStrategy strategy = new ExponentialBackoffStrategy(3, 2, 1000, 20_000, 0.0f);
        assertTrue(strategy.canRetry(1));
        assertEquals(1000, strategy.getDelayMillis(1));
        assertTrue(strategy.canRetry(1));
        assertEquals(2000, strategy.getDelayMillis(2));
        assertTrue(strategy.canRetry(2));
        assertEquals(4000, strategy.getDelayMillis(3));
        assertTrue(strategy.canRetry(3));
        assertFalse(strategy.canRetry(4));

    }
    @Test
    public void testDefaultBehaviour() {
        RetryStrategy strategy = ExponentialBackoffStrategy.DEFAULT;
        assertTrue(strategy.canRetry(0));
        assertEquals(0, strategy.getDelayMillis(0), 0);

        assertTrue(strategy.canRetry(1));
        assertEquals(750, strategy.getDelayMillis(1), 250);
        assertTrue(strategy.getDelayMillis(1) >= 500);
        assertTrue(strategy.getDelayMillis(1) <= 1000);

        assertTrue(strategy.canRetry(2));
        assertEquals(1500, strategy.getDelayMillis(2), 500);
        assertTrue(strategy.getDelayMillis(2) >= 1000);
        assertTrue(strategy.getDelayMillis(2) <= 2000);

        assertTrue(strategy.canRetry(3));
        assertEquals(3000, strategy.getDelayMillis(3), 1000);
        assertTrue(strategy.getDelayMillis(3) >= 2000);
        assertTrue(strategy.getDelayMillis(3) <= 4000);

        assertFalse(strategy.canRetry(4));
        // It will still calculate the delay even though canRetry is false
        assertEquals(6000, strategy.getDelayMillis(4), 2000);
    }

    @Test
    public void testMaxBackoffBehaviour() {
        ExponentialBackoffStrategy strategy = ExponentialBackoffStrategy.builder().setMaxAttempts(7).build();
        assertTrue(strategy.canRetry(0));
        assertEquals(0, strategy.getDelayMillis(0), 0);

        assertTrue(strategy.canRetry(5));
        assertTrue(strategy.getDelayMillis(5) >= 8000);
        assertTrue(strategy.getDelayMillis(5) <= 16_000);

        assertTrue(strategy.canRetry(6));
        assertTrue(strategy.getDelayMillis(6) >= 16_000);
        assertTrue(strategy.getDelayMillis(6) <= 20_000);

        assertTrue(strategy.canRetry(7));
        assertEquals(strategy.getDelayMillis(7), 20_000);

        assertFalse(strategy.canRetry(8));
        // It will still calculate the delay even though canRetry is false.
        assertEquals(strategy.getDelayMillis(8), 20_000);
    }

    @Test
    public void testCustomStrategy() {
        RetryStrategy strategy = new ExponentialBackoffStrategy(4, 4, 100, 2000, 0.1f);

        assertTrue(strategy.canRetry(1));
        assertTrue(strategy.getDelayMillis(1) >= 90);
        assertTrue(strategy.getDelayMillis(1) <= 100);

        assertTrue(strategy.canRetry(2));
        assertTrue(strategy.getDelayMillis(2) >= 360);
        assertTrue(strategy.getDelayMillis(2) <= 400);

        assertTrue(strategy.canRetry(3));
        assertTrue(strategy.getDelayMillis(3) >= 1200);
        assertTrue(strategy.getDelayMillis(3) <= 1600);

        assertTrue(strategy.canRetry(4));
        assertEquals(2000, strategy.getDelayMillis(4));

        // It will still calculate the delay even though canRetry is false
        assertFalse(strategy.canRetry(5));
    }

}
