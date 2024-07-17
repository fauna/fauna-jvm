package com.fauna.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRetryHandler {
    @Test
    public void testDefaultHandler() {
        RetryHandler handler = new RetryHandler(ExponentialBackoffStrategy.builder().build());
        assertEquals(1125.0, handler.getDelayMillis(), 125);
        assertEquals(2250.0, handler.getDelayMillis(), 250.0);
    }
}
