package com.fauna.client;

import org.junit.jupiter.api.Test;

public class TestRetryHandler {
    @Test
    public void testNoRetries() {
        RetryHandler handler = RetryHandler.noRetries();
        handler.getDelayMillis();
    }
}
