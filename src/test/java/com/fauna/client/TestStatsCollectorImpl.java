package com.fauna.client;

import com.fauna.response.QueryStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestStatsCollectorImpl {

    private StatsCollectorImpl statsCollector;

    @BeforeEach
    public void setUp() {
        statsCollector = new StatsCollectorImpl();
    }

    @Test
    public void testAdd_singleQueryStats_updatesCorrectly() {
        // Arrange
        QueryStats stats = new QueryStats(
                10,
                20,
                5,
                100,
                1,
                500,
                300,
                50,
                Arrays.asList("read", "compute")
        );

        // Act
        statsCollector.add(stats);

        // Assert
        QueryStatsSummary result = statsCollector.read();
        assertEquals(10, result.getComputeOps());
        assertEquals(20, result.getReadOps());
        assertEquals(5, result.getWriteOps());
        assertEquals(100, result.getQueryTimeMs());
        assertEquals(1, result.getContentionRetries());
        assertEquals(500, result.getStorageBytesRead());
        assertEquals(300, result.getStorageBytesWrite());
        assertEquals(50, result.getProcessingTimeMs());
        assertEquals(1, result.getQueryCount());
        assertEquals(1, result.getRateLimitedReadQueryCount());
        assertEquals(1, result.getRateLimitedComputeQueryCount());
        assertEquals(0, result.getRateLimitedWriteQueryCount());
    }

    @Test
    public void testAdd_multipleQueryStats_accumulatesValuesCorrectly() {
        // Arrange
        QueryStats stats1 = new QueryStats(10, 20, 5, 100, 1, 500, 300, 30,
                Collections.singletonList("read"));
        QueryStats stats2 = new QueryStats(15, 25, 10, 200, 2, 600, 400, 40,
                Collections.singletonList("write"));

        // Act
        statsCollector.add(stats1);
        statsCollector.add(stats2);

        // Assert
        QueryStatsSummary result = statsCollector.read();
        assertEquals(25, result.getComputeOps());
        assertEquals(45, result.getReadOps());
        assertEquals(15, result.getWriteOps());
        assertEquals(300, result.getQueryTimeMs());
        assertEquals(3, result.getContentionRetries());
        assertEquals(1100, result.getStorageBytesRead());
        assertEquals(700, result.getStorageBytesWrite());
        assertEquals(70, result.getProcessingTimeMs());
        assertEquals(2, result.getQueryCount());
        assertEquals(1, result.getRateLimitedReadQueryCount());
        assertEquals(0, result.getRateLimitedComputeQueryCount());
        assertEquals(1, result.getRateLimitedWriteQueryCount());
    }

    @Test
    public void testRead_initialStats_returnsZeroStats() {
        // Act
        QueryStatsSummary result = statsCollector.read();

        // Assert
        assertEquals(0, result.getComputeOps());
        assertEquals(0, result.getReadOps());
        assertEquals(0, result.getWriteOps());
        assertEquals(0, result.getQueryTimeMs());
        assertEquals(0, result.getContentionRetries());
        assertEquals(0, result.getStorageBytesRead());
        assertEquals(0, result.getStorageBytesWrite());
        assertEquals(0, result.getProcessingTimeMs());
        assertEquals(0, result.getQueryCount());
        assertEquals(0, result.getRateLimitedReadQueryCount());
        assertEquals(0, result.getRateLimitedComputeQueryCount());
        assertEquals(0, result.getRateLimitedWriteQueryCount());
    }

    @Test
    public void testReadAndReset_returnsAndResetsStats() {
        // Arrange
        QueryStats stats = new QueryStats(
                10, 20, 5, 100, 1, 500, 300, 75, Arrays.asList("read", "write")
        );
        statsCollector.add(stats);

        // Act
        QueryStatsSummary beforeReset = statsCollector.readAndReset();
        QueryStatsSummary afterReset = statsCollector.read();

        // Assert the stats before reset
        assertEquals(10, beforeReset.getComputeOps());
        assertEquals(20, beforeReset.getReadOps());
        assertEquals(5, beforeReset.getWriteOps());
        assertEquals(100, beforeReset.getQueryTimeMs());
        assertEquals(1, beforeReset.getContentionRetries());
        assertEquals(500, beforeReset.getStorageBytesRead());
        assertEquals(300, beforeReset.getStorageBytesWrite());
        assertEquals(75, beforeReset.getProcessingTimeMs());
        assertEquals(1, beforeReset.getQueryCount());
        assertEquals(1, beforeReset.getRateLimitedReadQueryCount());
        assertEquals(0, beforeReset.getRateLimitedComputeQueryCount());
        assertEquals(1, beforeReset.getRateLimitedWriteQueryCount());

        // Assert the stats after reset
        assertEquals(0, afterReset.getReadOps());
        assertEquals(0, afterReset.getComputeOps());
        assertEquals(0, afterReset.getWriteOps());
        assertEquals(0, afterReset.getQueryTimeMs());
        assertEquals(0, afterReset.getContentionRetries());
        assertEquals(0, afterReset.getStorageBytesRead());
        assertEquals(0, afterReset.getStorageBytesWrite());
        assertEquals(0, afterReset.getProcessingTimeMs());
        assertEquals(0, afterReset.getQueryCount());
        assertEquals(0, afterReset.getRateLimitedReadQueryCount());
        assertEquals(0, afterReset.getRateLimitedComputeQueryCount());
        assertEquals(0, afterReset.getRateLimitedWriteQueryCount());
    }
}