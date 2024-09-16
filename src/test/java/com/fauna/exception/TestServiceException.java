package com.fauna.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fauna.response.QueryFailure;
import com.fauna.response.wire.QueryResponseWire;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestServiceException {
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testNullResponseThrowsNullPointer() {
        assertThrows(NullPointerException.class, () -> new ServiceException(null));
    }

    @Test
    public void testGetters() throws IOException {
        // Given
        ObjectNode root = mapper.createObjectNode();
        root.put("summary", "summarized");
        root.put("schema_version", 10);
        root.put("query_tags", "foo=bar");
        root.put("txn_ts", Long.MAX_VALUE / 4); // MAX_VALUE would cause overflow

        ObjectNode error = root.putObject("error");
        error.put("code", "bad_thing");
        error.put("message", "message in a bottle");

        ObjectNode stats = root.putObject("stats");
        stats.put("compute_ops", 100);

        QueryResponseWire res = mapper.readValue(root.toString(), QueryResponseWire.class);
        QueryFailure failure = new QueryFailure(500, res);

        // When
        ServiceException exc = new ServiceException(failure);

        // Then
        assertEquals(500, exc.getStatusCode());
        assertEquals("bad_thing", exc.getErrorCode());
        assertEquals("500 (bad_thing): message in a bottle\n---\nsummarized", exc.getMessage());
        assertEquals("summarized", exc.getSummary());
        assertEquals(100, exc.getStats().computeOps);
        assertEquals(10, exc.getSchemaVersion());
        assertEquals(Optional.of(Long.MAX_VALUE / 4), exc.getTxnTs());
        assertEquals(Map.of("foo", "bar"), exc.getQueryTags());

    }
}
