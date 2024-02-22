package com.fauna.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fauna.common.constants.ResponseFields;
import com.fauna.exception.SerializationException;
import com.fauna.interfaces.IDeserializer;
import com.fauna.mapping.MappingContext;
import java.util.HashMap;
import java.util.Map;

public abstract class QueryResponse {

    private JsonNode rawJson;
    private long lastSeenTxn;
    private long schemaVersion;
    private String summary = "";
    private Map<String, String> queryTags = new HashMap<>();
    private QueryStats stats;

    QueryResponse(JsonNode json) {
        rawJson = json;

        JsonNode elem;

        if ((elem = json.get(ResponseFields.LAST_SEEN_TXN_FIELD_NAME)) != null) {
            lastSeenTxn = elem.asLong();
        }

        if ((elem = json.get(ResponseFields.SCHEMA_VERSION_FIELD_NAME)) != null) {
            schemaVersion = elem.asLong();
        }

        if ((elem = json.get(ResponseFields.SUMMARY_FIELD_NAME)) != null) {
            summary = elem.asText();
        }

        if ((elem = json.get(ResponseFields.QUERY_TAGS_FIELD_NAME)) != null) {
            String queryTagsString = elem.asText();

            if (queryTagsString != null && !queryTagsString.isEmpty()) {
                String[] tagPairs = queryTagsString.split(",");
                for (String tagPair : tagPairs) {
                    String[] tokens = tagPair.split("=");
                    queryTags.put(tokens[0], tokens[1]);
                }
            }
        }

        if ((elem = json.get(ResponseFields.STATS_FIELD_NAME)) != null) {
            ObjectMapper mapper = new ObjectMapper();
            stats = mapper.convertValue(elem, QueryStats.class);
        }
    }

    /**
     * Asynchronously parses the HTTP response message to create a QueryResponse instance.
     *
     * @param <T>          The expected data type of the query response.
     * @param ctx          Serialization context for handling response data.
     * @param deserializer A deserializer for the success data type.
     * @param statusCode   The HTTP status code.
     * @param body         The response body.
     * @return A Task that resolves to a QueryResponse instance.
     */
    public static <T> QueryResponse getFromResponseBody(
        MappingContext ctx,
        IDeserializer<T> deserializer,
        int statusCode,
        String body) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(body);

            if (statusCode >= 200
                && statusCode <= 299) {
                return new QuerySuccess<>(ctx, deserializer, json);
            }

            return new QueryFailure(statusCode, json);
        } catch (Exception e) {
            throw new SerializationException("Error occurred while parsing the response body", e);
        }
    }

    public JsonNode getRawJson() {
        return rawJson;
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

