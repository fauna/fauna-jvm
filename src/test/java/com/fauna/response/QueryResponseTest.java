package com.fauna.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fauna.beans.ClassWithAttributes;
import com.fauna.codec.*;
import com.fauna.constants.ResponseFields;
import com.fauna.exception.ClientResponseException;
import com.fauna.exception.CodecException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.fauna.exception.ProtocolException;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.response.wire.QueryResponseWire;
import org.junit.jupiter.api.Test;

class QueryResponseTest {

    CodecRegistry codecRegistry = new DefaultCodecRegistry();
    CodecProvider codecProvider = new DefaultCodecProvider(codecRegistry);

    static HttpResponse<InputStream> mockResponse(String body) {
        HttpResponse resp = mock(HttpResponse.class);
        doAnswer(invocationOnMock -> new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8))).when(resp).body();
        return resp;
    }

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
        HttpResponse resp = mockResponse(body);
        when(resp.statusCode()).thenReturn(200);

        QuerySuccess<ClassWithAttributes> success = QueryResponse.parseResponse(resp, codec);

        assertEquals(baz.getFirstName(), success.getData().getFirstName());
        assertEquals("PersonWithAttributes", success.getStaticType().get());


    }

    @Test
    public void handleResponseWithInvalidJsonThrowsProtocolException() {
        HttpResponse resp = mockResponse("{\"not valid json\"");
        when(resp.statusCode()).thenReturn(400);

        ClientResponseException exc = assertThrows(ClientResponseException.class, () -> QueryResponse.parseResponse(resp, codecProvider.get(Object.class)));
        assertEquals("ClientResponseException HTTP 400: Failed to handle error response.", exc.getMessage());
    }

    @Test
    public void handleResponseWithMissingStatsThrowsProtocolException() {
        HttpResponse resp = mockResponse("{\"not valid json\"");
        assertThrows(ClientResponseException.class, () -> QueryResponse.parseResponse(resp,  codecProvider.get(Object.class)));
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