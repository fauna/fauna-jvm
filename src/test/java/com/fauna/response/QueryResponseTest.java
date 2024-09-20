package com.fauna.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fauna.beans.ClassWithAttributes;
import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;
import com.fauna.codec.CodecRegistry;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.DefaultCodecRegistry;
import com.fauna.exception.ClientResponseException;
import com.fauna.exception.CodecException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.fauna.codec.UTF8FaunaGenerator;
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
    public void handleResponseWithInvalidJsonThrowsClientResponseException() {
        HttpResponse resp = mockResponse("{\"not valid json\"");
        when(resp.statusCode()).thenReturn(400);

        ClientResponseException exc = assertThrows(ClientResponseException.class, () -> QueryResponse.parseResponse(resp, codecProvider.get(Object.class)));
        assertEquals("ClientResponseException HTTP 400: Failed to handle error response.", exc.getMessage());
    }

    @Test
    public void handleResponseWithEmptyFieldsDoesNotThrow() {
        HttpResponse resp = mockResponse("{}");
        QuerySuccess<Object> response = QueryResponse.parseResponse(resp,  codecProvider.get(Object.class));
        assertEquals(QuerySuccess.class, response.getClass());
        assertNull(response.getSchemaVersion());
        assertNull(response.getSummary());
        assertNull(response.getLastSeenTxn());
        assertNull(response.getQueryTags());
        assertNull(response.getData());
        assertNull(response.getStats());
    }
}