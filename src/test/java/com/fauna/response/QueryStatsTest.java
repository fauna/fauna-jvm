package com.fauna.response;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class QueryStatsTest {
    static final ObjectMapper MAPPER = new ObjectMapper();
    static final JsonFactory FACTORY = new JsonFactory();
    @Test
    public void testQueryStatsStringValue() {
        QueryStats stats = new QueryStats(1, 2, 3, 4, 5, 6, 7, 8, List.of("a", "b"));
        assertEquals("compute: 1, read: 2, write: 3, queryTime: 4, retries: 5, storageRead: 6, storageWrite: 7, limits: [a, b]",
                stats.toString());
    }

    @Test
    public void testParseQueryStats() throws IOException {
        ObjectNode statsNode = MAPPER.createObjectNode();
        statsNode.put("compute_ops", 1);
        statsNode.put("read_ops", 2);
        statsNode.put("write_ops", 3);
        statsNode.put("query_time_ms", 4);
        statsNode.put("contention_retries", 5);
        statsNode.put("storage_bytes_read", 6);
        statsNode.put("storage_bytes_write", 7);
        ArrayNode limits = statsNode.putArray("rate_limits_hit");
        limits.add("a");
        limits.add("b");
        QueryStats stats = QueryStats.parseStats(FACTORY.createParser(statsNode.toString().getBytes()));
        assertEquals("compute: 1, read: 2, write: 3, queryTime: 4, retries: 5, storageRead: 6, storageWrite: 7, limits: [a, b]", stats.toString());
    }

    @Test
    public void testParseNullStats() throws IOException {
        JsonParser parser = FACTORY.createParser("{\"stats\": null}".getBytes());
        parser.nextToken();
        QueryStats stats = QueryStats.parseStats(parser);
        assertNull(stats);
    }
}
