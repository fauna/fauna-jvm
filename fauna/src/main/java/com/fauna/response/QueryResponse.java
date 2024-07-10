package com.fauna.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fauna.common.constants.ResponseFields;

import java.util.HashMap;
import java.util.Map;

public abstract class QueryResponse {

    private final JsonNode rawJson;
    private long lastSeenTxn;
    private long schemaVersion;
    private String summary = "";
    private final Map<String, String> queryTags = new HashMap<>();
    private QueryStats stats;

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

