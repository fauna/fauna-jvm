package com.fauna.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fauna.response.QueryFailure;
import com.fauna.response.QueryResponse;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestAbortException {
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testAbortDataObject() throws JsonProcessingException {
        // Given
        ObjectNode root = mapper.createObjectNode();
        ObjectNode error = root.putObject("error");
        error.put("code", "abort");
        ObjectNode abort = error.putObject("abort");
        abort.put("some", "reason");

        QueryFailure failure = new QueryFailure(500, root, QueryResponse.DEFAULT_STATS);

        // When
        AbortException exc = new AbortException(failure);

        // Then
        HashMap<String, String>  expected = new HashMap<>();
        expected.put("some", "reason");
        assertEquals(expected, exc.getAbort());
    }

    @Test
    public void testAbortDataString() throws JsonProcessingException {
        // Given
        ObjectNode root = mapper.createObjectNode();
        ObjectNode error = root.putObject("error");
        error.put("code", "abort");
        error.put("abort", "some reason");
        QueryFailure failure = new QueryFailure(500, root, QueryResponse.DEFAULT_STATS);

        // When
        AbortException exc = new AbortException(failure);

        // Then
        assertEquals("some reason", exc.getAbort());
    }

    @Test
    public void testAbortDataMissing() throws JsonProcessingException {
        // Given
        ObjectNode root = mapper.createObjectNode();
        ObjectNode error = root.putObject("error");
        error.put("code", "abort");
        QueryFailure failure = new QueryFailure(500, root, QueryResponse.DEFAULT_STATS);

        // When
        AbortException exc = new AbortException(failure);

        // Then
        assertThrows(RuntimeException.class, () -> exc.getAbort());
    }
}
