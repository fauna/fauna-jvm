package com.fauna.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

    @ParameterizedTest
    @ValueSource(strings = {"1", "DEBUG", "2", "foo", "0.0", " 1", " 1000 "})
    public void testDebugLogVals(String val) {
        assertEquals(Level.FINE, FaunaConfig.Builder.getLogLevel(val));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", " ", "-1", "", "\n", " \r \n \t"})
    public void testWarningLogVals(String val) {
        assertEquals(Level.WARNING, FaunaConfig.Builder.getLogLevel(val));
    }
}
