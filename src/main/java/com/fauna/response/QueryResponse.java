package com.fauna.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fauna.codec.Codec;
import com.fauna.exception.ErrorHandler;
import com.fauna.exception.FaunaException;
import com.fauna.exception.ProtocolException;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class QueryResponse {

    private static final ObjectMapper mapper = new ObjectMapper();
    public static final QueryStats DEFAULT_STATS = new QueryStats(0, 0, 0, 0, 0, 0, 0, List.of());

    private final long lastSeenTxn;
    private final long schemaVersion;
    private final String summary;
    private final Map<String, String> queryTags = new HashMap<>();
    private final QueryStats stats;

    QueryResponse(QueryResponseInternal response) {

        lastSeenTxn = response.txnTs;
        schemaVersion = response.schemaVersion;
        summary = response.summary;
        stats = response.stats;

        if (response.queryTags != null && !response.queryTags.isEmpty()) {
            String[] tagPairs = response.queryTags.split(",");
            for (String tagPair : tagPairs) {
                String[] tokens = tagPair.split("=");
                queryTags.put(tokens[0], tokens[1]);
            }
        }
    }

    /**
     * Handle a HTTPResponse and return a QuerySucces, or throw a FaunaException.
     * @param response          The HTTPResponse object.
     * @return                  A successful response from Fauna.
     * @throws FaunaException
     */
    public static <T>  QuerySuccess<T> handleResponse(HttpResponse<String> response, Codec<T> codec) throws FaunaException {
        String body = response.body();
        try {
            var responseInternal = mapper.readValue(body, QueryResponseInternal.class);
            if (response.statusCode() >= 400) {
                ErrorHandler.handleErrorResponse(response.statusCode(), responseInternal);
            }

            return new QuerySuccess<>(codec, responseInternal);
        } catch (IOException exc) { // Jackson JsonProcessingException subclasses IOException
            throw new ProtocolException(exc, response.statusCode(), body);
        }
    }

    public long getLastSeenTxn() {
        return lastSeenTxn;
    }

    public long getSchemaVersion() {
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

