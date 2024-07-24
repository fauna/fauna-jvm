package com.fauna.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fauna.constants.ResponseFields;
import com.fauna.exception.ErrorHandler;
import com.fauna.exception.FaunaException;
import com.fauna.exception.ProtocolException;
import com.fauna.serialization.Deserializer;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class QueryResponse {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final JsonNode rawJson;
    private long lastSeenTxn;
    private long schemaVersion;
    private String summary = "";
    private final Map<String, String> queryTags = new HashMap<>();
    private QueryStats stats;
    public static final QueryStats DEFAULT_STATS = new QueryStats(0, 0, 0, 0, 0, 0, 0, List.of());

    QueryResponse(JsonNode json, QueryStats stats) {
        this.rawJson = json;
        this.stats = stats;

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

    }

    public static QueryResponse handleResponse(HttpResponse<String> response) throws FaunaException {
        String body = response.body();
        try {
            if (response.statusCode() >= 400) {
                ErrorHandler.handleErrorResponse(response.statusCode(), body, mapper);
            }
            JsonNode json = mapper.readTree(response.body());
            JsonNode statsNode = json.get(ResponseFields.STATS_FIELD_NAME);
            if (statsNode != null) {
                QueryStats stats = mapper.convertValue(statsNode, QueryStats.class);
                return new QuerySuccess<>(Deserializer.DYNAMIC, json, stats);
            } else {
                throw new ProtocolException(response.statusCode(), body);
            }
        } catch (IOException exc) { // Jackson JsonProcessingException subclasses IOException
            throw new ProtocolException(exc, response.statusCode(), body);
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

