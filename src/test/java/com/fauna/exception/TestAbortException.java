package com.fauna.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fauna.response.ErrorInfo;
import com.fauna.response.QueryFailure;
import com.fauna.response.QueryResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestAbortException {
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testAbortDataObject() throws IOException {
        // Given
        ObjectNode abort = mapper.createObjectNode();
        ObjectNode num = abort.putObject("num");
        num.put("@int", "42");

        QueryFailure failure = new QueryFailure(500, QueryResponse.builder(null).error(ErrorInfo.builder().code("abort").abort(abort).build()));

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
        QueryFailure failure = new QueryFailure(500,
                QueryResponse.builder(null).error(
                        ErrorInfo.builder().code("abort").abort(
                                TextNode.valueOf("some reason")).build()));

        // When
        AbortException exc = new AbortException(failure);

        // Then
        assertEquals("some reason", exc.getAbort());
    }

    @Test
    public void testAbortDataMissing() throws IOException {
        // Given
        QueryFailure failure = new QueryFailure(200,
                QueryResponse.builder(null).error(
                        ErrorInfo.builder().code("abort").message("some message").build()));

        // When
        AbortException exc = new AbortException(failure);

        // Then
        assertNull(exc.getAbort());
    }
}
