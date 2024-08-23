package com.fauna.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fauna.response.ErrorInfoInternal;
import com.fauna.response.QueryFailure;
import com.fauna.response.QueryResponse;
import com.fauna.response.QueryResponseInternal;
import org.junit.jupiter.api.Test;

import javax.management.Query;
import java.io.IOException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        var res = mapper.readValue(root.toString(), QueryResponseInternal.class);

        QueryFailure failure = new QueryFailure(500, res);

        // When
        AbortException exc = new AbortException(failure);

        // Then
        HashMap<String, Integer>  expected = new HashMap<>();
        expected.put("num", 42);
        assertEquals(expected, exc.getAbort());

        // Assert caching
        assertSame(exc.getAbort(), exc.getAbort());
    }

    @Test
    public void testAbortDataString() throws IOException {
        // Given
        ObjectNode root = mapper.createObjectNode();
        ObjectNode error = root.putObject("error");
        error.put("code", "abort");
        error.put("abort", "some reason");
        var res = mapper.readValue(root.toString(), QueryResponseInternal.class);
        QueryFailure failure = new QueryFailure(500, res);

        // When
        AbortException exc = new AbortException(failure);

        // Then
        assertEquals("some reason", exc.getAbort());
    }

    @Test
    public void testAbortDataMissing() throws IOException {
        // Given
        ObjectNode root = mapper.createObjectNode();
        ObjectNode error = root.putObject("error");
        error.put("code", "abort");
        var res = mapper.readValue(root.toString(), QueryResponseInternal.class);
        QueryFailure failure = new QueryFailure(500, res);

        // When
        AbortException exc = new AbortException(failure);

        // Then
        assertNull(exc.getAbort());
    }
}
