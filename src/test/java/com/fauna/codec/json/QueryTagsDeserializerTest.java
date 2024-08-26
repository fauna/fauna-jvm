package com.fauna.codec.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fauna.response.wire.QueryResponseWire;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class QueryTagsDeserializerTest {

    @Test
    public void deserializeNull() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var json = "{\"query_tags\":null}";
        var res = mapper.readValue(json, QueryResponseWire.class);
        Assertions.assertNull(res.getQueryTags());
    }

    @Test
    public void deserializeEmptyString() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var json = "{\"query_tags\":\"\"}";
        var res = mapper.readValue(json, QueryResponseWire.class);
        Assertions.assertTrue(res.getQueryTags().isEmpty());
    }

    @Test
    public void deserializeQueryTags() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var json = "{\"query_tags\":\"foo=bar,baz=foo\"}";
        var res = mapper.readValue(json, QueryResponseWire.class);
        Assertions.assertNotNull(res.getQueryTags());
        Assertions.assertEquals(Map.of("foo", "bar", "baz", "foo"), res.getQueryTags());
    }
}
