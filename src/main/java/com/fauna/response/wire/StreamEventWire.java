package com.fauna.response.wire;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fauna.codec.json.PassThroughDeserializer;
import com.fauna.constants.ResponseFields;
import com.fauna.response.QueryStats;

public class StreamEventWire {

    @JsonProperty(ResponseFields.DATA_FIELD_NAME)
    @JsonDeserialize(using = PassThroughDeserializer.class)
    private String data;

    @JsonProperty(ResponseFields.LAST_SEEN_TXN_FIELD_NAME)
    private Long last_txn;

    @JsonProperty(ResponseFields.STREAM_EVENT_TYPE_FIELD_NAME)
    private String eventType;

    @JsonProperty(ResponseFields.STREAM_CURSOR_FIELD_NAME)
    private String cursor;

    @JsonProperty(ResponseFields.STATS_FIELD_NAME)
    private QueryStats stats;

    public String getEventType() {
        return this.eventType;
    }

    public String getData() {
        return data != null ? data : "null";
    }

    public long getTxnTs() {
        return this.last_txn;
    }

    public String getCursor() {
        return this.cursor;
    }

    public QueryStats getStats() {
        return stats;
    }
}
