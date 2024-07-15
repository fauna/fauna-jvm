package com.fauna.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fauna.response.QueryFailure;
import com.fauna.response.QueryResponse;
import com.fauna.response.QueryStats;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestServiceException {
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testNullResponseThrowsNullPointer() {
        assertThrows(NullPointerException.class, () -> new ServiceException(null));
    }

    @Test
    public void testGetters() throws JsonProcessingException {
        // Given
        ObjectNode root = mapper.createObjectNode();
        ObjectNode error = root.putObject("error");
        error.put("code", "bad_thing");
        error.put("message", "message in a bottle");

        root.put("summary", "summarized");
        root.put("schema_version", 10);
        root.put("query_tags", "foo=bar");
        root.put("txn_ts", Long.MAX_VALUE / 4); // would cause int overflow
        QueryFailure failure = new QueryFailure(500, root, QueryResponse.DEFAULT_STATS);

        // When
        ServiceException exc = new ServiceException(failure);

        // Then
        assertEquals(500, exc.getStatusCode());
        assertEquals("bad_thing", exc.getErrorCode());
        assertEquals("message in a bottle", exc.getMessage());
        assertEquals("summarized", exc.getSummary());
        assertEquals(0, exc.getStats().computeOps);
        assertEquals(10, exc.getSchemaVersion());
        assertEquals(Long.MAX_VALUE / 4, exc.getTxnTs());
        assertEquals(Map.of("foo", "bar"), exc.getQueryTags());

    }
}
