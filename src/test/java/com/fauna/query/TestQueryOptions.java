package com.fauna.query;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestQueryOptions {

    @Test
    public void testDefaults() {
        QueryOptions options = QueryOptions.builder().build();
        assertTrue(options.getLinearized().isEmpty());
        assertTrue(options.getTypeCheck().isEmpty());
        assertTrue(options.getQueryTags().isEmpty());
        assertTrue(options.getTraceParent().isEmpty());

        assertEquals(5_000, options.getTimeoutMillis().get());
    }

    @Test
    public void testAllOptions() {
        QueryOptions options = QueryOptions.builder()
                .linearized(false).typeCheck(true)
                .traceParent("parent").timeout(Duration.ofMinutes(5))
                .queryTag("hello", "world")
                .build();
        assertEquals(false, options.getLinearized().get());
        assertEquals(true, options.getTypeCheck().get());
        assertEquals("parent", options.getTraceParent().get());
        assertEquals(5 * 60 * 1000, options.getTimeoutMillis().get());
        assertEquals("world", options.getQueryTags().get().get("hello"));
    }

    @Test
    public void testQueryTagsBuilderMethods() {
        QueryTags initialTags = new QueryTags();
        initialTags.put("foo", "bar");
        QueryOptions opts = QueryOptions.builder().queryTags(initialTags)
                .queryTag("hello", "world").build();
        assertEquals("foo=bar,hello=world",
                opts.getQueryTags().orElseThrow().encode());
    }

    @Test
    public void testQueryTagsBuilderMethodsAreAdditive() {
        QueryTags initialTags = new QueryTags();
        initialTags.put("foo", "bar");
        QueryOptions opts = QueryOptions.builder().queryTag("hello", "world")
                .queryTags(initialTags).build();
        assertEquals("foo=bar,hello=world",
                opts.getQueryTags().orElseThrow().encode());
    }
}
