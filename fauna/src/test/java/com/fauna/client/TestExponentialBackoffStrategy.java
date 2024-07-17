package com.fauna.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestExponentialBackoffStrategy {

    @Test
    public void testInitialRequestIsNotDelayed() {
        ExponentialBackoffStrategy strategy = ExponentialBackoffStrategy.builder().build();
        assertEquals(0, strategy.getDelayMillis(System.currentTimeMillis(), 0));

    }
    @Test
    public void testDefaultBehaviour() {
        ExponentialBackoffStrategy strategy = ExponentialBackoffStrategy.builder().build();
        assertEquals(0, strategy.getDelayMillis(System.currentTimeMillis(),0), 0);
        assertEquals(1125.0, strategy.getDelayMillis(System.currentTimeMillis(),1), 125);
        assertEquals(2250.0, strategy.getDelayMillis(System.currentTimeMillis(), 2), 250);
    }


}
