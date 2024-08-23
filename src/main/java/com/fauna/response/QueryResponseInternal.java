package com.fauna.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fauna.codec.Codec;
import com.fauna.codec.json.PassThroughDeserializer;

public class QueryResponseInternal {

    @JsonProperty(ResponseFields.DATA_FIELD_NAME)
    @JsonDeserialize(using = PassThroughDeserializer.class)
    public String data;

    @JsonProperty(ResponseFields.ERROR_FIELD_NAME)
    public ErrorInfoInternal error;

    @JsonProperty(ResponseFields.QUERY_TAGS_FIELD_NAME)
    public String queryTags;

    @JsonProperty(ResponseFields.SCHEMA_VERSION_FIELD_NAME)
    public long schemaVersion;

    @JsonProperty(ResponseFields.STATS_FIELD_NAME)
    public QueryStats stats;

    @JsonProperty(ResponseFields.STATIC_TYPE_FIELD_NAME)
    public String staticType;

    @JsonProperty(ResponseFields.SUMMARY_FIELD_NAME)
    public String summary;

    @JsonProperty(ResponseFields.TXN_TS_FIELD_NAME)
    public long txnTs;
}

