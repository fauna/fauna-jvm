package com.fauna.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fauna.codec.Codec;
import com.fauna.exception.ErrorHandler;
import com.fauna.exception.FaunaException;
import com.fauna.exception.ProtocolException;
import com.fauna.response.wire.QueryResponseWire;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;

public abstract class QueryResponse {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final Long lastSeenTxn;
    private final Long schemaVersion;
    private final String summary;
    private final Map<String, String> queryTags;
    private final QueryStats stats;

    QueryResponse(QueryResponseWire response) {

        lastSeenTxn = response.getTxnTs();
        schemaVersion = response.getSchemaVersion();
        summary = response.getSummary();
        stats = response.getStats();
        queryTags = response.getQueryTags();
    }

    /**
     * Handle a HTTPResponse and return a QuerySuccess, or throw a FaunaException.
     * @param response          The HTTPResponse object.
     * @return                  A successful response from Fauna.
     * @throws FaunaException
     */
    public static <T>  QuerySuccess<T> handleResponse(HttpResponse<String> response, Codec<T> codec) throws FaunaException {
        String body = response.body();
        try {
            var responseInternal = mapper.readValue(body, QueryResponseWire.class);
            if (response.statusCode() >= 400) {
                ErrorHandler.handleErrorResponse(response.statusCode(), responseInternal, body);
            }
            return new QuerySuccess<>(codec, responseInternal);
        } catch (IOException exc) { // Jackson JsonProcessingException subclasses IOException
            throw new ProtocolException(exc, response.statusCode(), body);
        }
    }

    public Long getLastSeenTxn() {
        return lastSeenTxn;
    }

    public Long getSchemaVersion() {
        return schemaVersion;
    }

    public String getSummary() {
        return summary;
    }

    public Map<String, String> getQueryTags() {
        return queryTags;
    }

    public QueryStats getStats() {
        return stats;
    }
}

