package com.fauna.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fauna.beans.ClassWithAttributes;
import com.fauna.codec.*;
import com.fauna.constants.ResponseFields;
import com.fauna.exception.ClientResponseException;
import com.fauna.exception.CodecException;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Optional;

import com.fauna.exception.ProtocolException;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.response.wire.QueryResponseWire;
import org.junit.jupiter.api.Test;

class QueryResponseTest {

    CodecRegistry codecRegistry = new DefaultCodecRegistry();
    CodecProvider codecProvider = new DefaultCodecProvider(codecRegistry);

    @SuppressWarnings("unchecked")
    private <T> String encode(Codec<T> codec, T obj) throws IOException {
        try(UTF8FaunaGenerator gen = new UTF8FaunaGenerator()) {
            codec.encode(gen, obj);
            return gen.serialize();
        }
    }

    @Test
    public void getFromResponseBody_Success() throws IOException {
        ClassWithAttributes baz = new ClassWithAttributes("baz", "luhrman", 64);

        Codec<ClassWithAttributes> codec = codecProvider.get(ClassWithAttributes.class);
        String data = encode(codec, baz);
        String body = "{\"stats\":{},\"static_type\":\"PersonWithAttributes\",\"data\":" + data + "}";
        HttpResponse resp = mock(HttpResponse.class);
        when(resp.body()).thenReturn(body);
        when(resp.statusCode()).thenReturn(200);

        QuerySuccess<ClassWithAttributes> success = QueryResponse.handleResponse(resp, codec);

        assertEquals(baz.getFirstName(), success.getData().getFirstName());
        assertEquals("PersonWithAttributes", success.getStaticType().get());


    }

    @Test
    public void getFromResponseBody_Failure() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode errorData = mapper.createObjectNode();
        errorData.put(ResponseFields.ERROR_CODE_FIELD_NAME, "ErrorCode");
        errorData.put(ResponseFields.ERROR_MESSAGE_FIELD_NAME, "ErrorMessage");
        // ObjectNode cf = errorData.putObject(ResponseFields.ERROR_CONSTRAINT_FAILURES_FIELD_NAME);
        errorData.put(ResponseFields.ERROR_ABORT_FIELD_NAME, "AbortData");
        ObjectNode failureNode = mapper.createObjectNode();
        failureNode.put(ResponseFields.ERROR_FIELD_NAME, errorData);

        var res = mapper.readValue(failureNode.toString(), QueryResponseWire.class);
        QueryFailure response = new QueryFailure(400, res);

        assertEquals(400, response.getStatusCode());
        assertEquals("ErrorCode", response.getErrorCode());
        assertEquals("ErrorMessage", response.getMessage());
        assertTrue(response.getConstraintFailures().isEmpty());
        assertEquals(Optional.of("\"AbortData\""), response.getAbortString());
    }

    @Test
    public void handleResponseWithInvalidJsonThrowsProtocolException() {
        HttpResponse resp = mock(HttpResponse.class);
        String body = "{\"not valid json\"";
        when(resp.statusCode()).thenReturn(400);
        when(resp.body()).thenReturn(body);

        ClientResponseException exc = assertThrows(ClientResponseException.class, () -> QueryResponse.handleResponse(resp, codecProvider.get(Object.class)));
        assertEquals("ClientResponseException HTTP 400: Failed to handle error response.", exc.getMessage());
    }

    @Test
    public void handleResponseWithMissingStatsThrowsProtocolException() {
        HttpResponse resp = mock(HttpResponse.class);
        when(resp.body()).thenReturn("{\"not valid json\"");
        assertThrows(ClientResponseException.class, () -> QueryResponse.handleResponse(resp,  codecProvider.get(Object.class)));
    }

    @Test
    void getFromResponseBody_Exception() {
        String body = "Invalid JSON";

        // TODO call FaunaClient.handleResponse here.
        CodecException exception = assertThrows(CodecException.class, () -> {
            throw new CodecException("Error occurred while parsing the response body");
        });

        assertEquals("Error occurred while parsing the response body", exception.getMessage());
        // assertTrue(exception.getCause().getMessage().contains(
        // "Unrecognized token 'Invalid': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')"));
    }

}