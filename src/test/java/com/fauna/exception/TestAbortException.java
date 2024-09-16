package com.fauna.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fauna.response.ErrorInfo;
import com.fauna.response.QueryFailure;
import com.fauna.response.QueryResponse;
import com.fauna.response.wire.QueryResponseWire;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class TestAbortException {
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testAbortDataObject() throws IOException {
        // Given
        ObjectNode root = mapper.createObjectNode();
        ObjectNode error = root.putObject("error");
        error.put("code", "abort");
        ObjectNode abort = error.putObject("abort");
        ObjectNode num = abort.putObject("num");
        num.put("@int", "42");
        var res = mapper.readValue(root.toString(), QueryResponseWire.class);

        QueryFailure failure = new QueryFailure(500, res);

        // When
        AbortException exc = new AbortException(failure);

        // Then
        HashMap<String, Integer>  expected = new HashMap<>();
        expected.put("num", 42);
        assertEquals(expected, exc.getAbort().orElseThrow());

        // Assert caching
        assertSame(exc.getAbort().orElseThrow(), exc.getAbort().orElseThrow());
    }

    @Test
    public void testAbortDataString() throws IOException {
        // Given
        ObjectNode root = mapper.createObjectNode();
        ObjectNode error = root.putObject("error");
        error.put("code", "abort");
        error.put("abort", "some reason");
        var res = mapper.readValue(root.toString(), QueryResponseWire.class);
        QueryFailure failure = new QueryFailure(500, res);

        // When
        AbortException exc = new AbortException(failure);

        // Then
        assertEquals("some reason", exc.getAbort().orElseThrow());
    }

    @Test
    public void testAbortDataMissing() throws IOException {
        // Given
        QueryResponse.Builder builder = QueryResponse.builder(null);
        builder.error(new ErrorInfo("abort", "some message", null, null));
        QueryFailure failure = new QueryFailure(200, builder);

        // When
        AbortException exc = new AbortException(failure);

        // Then
        assertTrue(exc.getAbort().isEmpty());
    }
}
