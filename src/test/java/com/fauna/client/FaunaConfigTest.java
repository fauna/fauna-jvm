package com.fauna.client;

import org.junit.jupiter.api.Test;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FaunaConfigTest {

    @Test
    public void testDefaultFaunaConfig() {
        // Running this test with FAUNA_ENDPOINT, FAUNA_SECRET, and FAUNA_DEBUG environment variables in an
        // IDE can be used to check that we set things correctly.
        FaunaConfig config = FaunaConfig.builder().build();
        assertEquals("https://db.fauna.com", config.getEndpoint());
        assertEquals(Level.WARNING, config.getLogHandler().getLevel());
        assertEquals("", config.getSecret());
        assertEquals(3, config.getMaxContentionRetries());
    }

    @Test
    public void testOverridingDefaultFaunaConfig() {
        // Running this test with FAUNA_ENDPOINT, FAUNA_SECRET, and FAUNA_DEBUG environment variables in an
        // IDE can be used to check that we set things correctly.
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        FaunaConfig config = FaunaConfig.builder()
                .secret("foo")
                .endpoint("endpoint")
                .logHandler(handler)
                .maxContentionRetries(1).build();
        assertEquals("endpoint", config.getEndpoint());
        assertEquals(Level.ALL, config.getLogHandler().getLevel());
        assertEquals("foo", config.getSecret());
        assertEquals(1, config.getMaxContentionRetries());
    }
}
