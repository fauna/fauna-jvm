package com.fauna.client;

import com.fauna.query.QueryTags;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryTagsTest {

    @Test
    void encode_shouldConvertMapToString() {
        QueryTags tags = new QueryTags();
        tags.put("key1", "value1");
        tags.put("key2", "value2");
        tags.put("key3", "value3");

        String result = tags.encode();

        assertEquals("key1=value1,key2=value2,key3=value3", result);
    }

    @Test
    void encode_shouldHandleEmptyMap() {
        QueryTags tags = new QueryTags();

        String result = tags.encode();

        assertEquals("", result);
    }

    @Test
    void encode_shouldHandleSingleEntry() {
        QueryTags tags = new QueryTags();
        tags.put("key1", "value1");


        String result = tags.encode();

        assertEquals("key1=value1", result);
    }

    @Test
    void encode_shouldHandleSpecialCharacters() {
        QueryTags tags = new QueryTags();
        tags.put("key1", "value1,value2");
        tags.put("key2", "value3,value4");

        String result = tags.encode();

        assertTrue(result.contains("key1=value1,value2"));
        assertTrue(result.contains("key2=value3,value4"));
    }

}