package com.fauna.response.wire;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fauna.codec.json.PassThroughDeserializer;
import com.fauna.codec.json.QueryTagsDeserializer;
import com.fauna.response.QueryStats;
import com.fauna.constants.ResponseFields;

import java.util.Map;

public class QueryResponseWire {

    @JsonProperty(ResponseFields.DATA_FIELD_NAME)
    @JsonDeserialize(using = PassThroughDeserializer.class)
    private String data;

    @JsonProperty(ResponseFields.ERROR_FIELD_NAME)
    private ErrorInfoWire error;

    @JsonProperty(ResponseFields.QUERY_TAGS_FIELD_NAME)
    @JsonDeserialize(using = QueryTagsDeserializer.class)
    private Map<String,String> queryTags;

    @JsonProperty(ResponseFields.SCHEMA_VERSION_FIELD_NAME)
    private Long schemaVersion;

    @JsonProperty(ResponseFields.STATS_FIELD_NAME)
    private QueryStats stats;

    @JsonProperty(ResponseFields.STATIC_TYPE_FIELD_NAME)
    private String staticType;

    @JsonProperty(ResponseFields.SUMMARY_FIELD_NAME)
    private String summary;

    @JsonProperty(ResponseFields.LAST_SEEN_TXN_FIELD_NAME)
    private Long txnTs;

    public String getData() {
        return data != null ? data : "null";
    }

    public ErrorInfoWire getError() {
        return error;
    }

    public Map<String, String> getQueryTags() {
        return queryTags;
    }

    public Long getSchemaVersion() {
        return schemaVersion;
    }

    public QueryStats getStats() {
        return stats;
    }

    public String getStaticType() {
        return staticType;
    }

    public String getSummary() {
        return summary;
    }

    public Long getTxnTs() {
        return txnTs;
    }
}

