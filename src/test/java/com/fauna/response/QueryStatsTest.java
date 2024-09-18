package com.fauna.response;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QueryStatsTest {
    @Test
    public void testQueryStatsStringValue() {
        QueryStats stats = new QueryStats(1, 2, 3, 4, 5, 6, 7, 8, List.of("a", "b"));
        assertEquals("compute: 1, read: 2, write: 3, queryTime: 4, retries: 5, storageRead: 6, storageWrite: 7, limits: [a, b]",
                stats.toString());
    }
}
