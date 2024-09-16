package com.fauna.response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fauna.codec.Codec;
import com.fauna.exception.ClientResponseException;
import com.fauna.exception.ErrorHandler;
import com.fauna.response.wire.QueryResponseWire;

import java.net.http.HttpResponse;

public class QueryResponseHandler {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> QueryResponse handle(HttpResponse<String> response, Codec<T> codec) {
        String body = response.body();
        try {
            if (response.statusCode() >= 400) {
                ErrorHandler.handleErrorResponse(response.statusCode(), null, body);
            }
            var responseInternal = mapper.readValue(body, QueryResponseWire.class);
            return new QuerySuccess<>(codec, responseInternal);
        } catch (JsonProcessingException exc) { // Jackson JsonProcessingException subclasses IOException
            throw new ClientResponseException("Failed to handle error response.", exc, response.statusCode());
        }

    }

    /*
    private static <T> QuerySuccess<T> parse(JsonParser parser) {

    } */

}
