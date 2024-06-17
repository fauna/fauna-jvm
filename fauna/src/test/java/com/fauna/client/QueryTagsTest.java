package com.fauna.client;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryTagsTest {

    @Test
    void encode_shouldConvertMapToString() {
        Map<String, String> tags = new HashMap<>();
        tags.put("key1", "value1");
        tags.put("key2", "value2");
        tags.put("key3", "value3");

        String result = QueryTags.encode(tags);

        assertTrue(result.contains("key1=value1"));
        assertTrue(result.contains("key2=value2"));
        assertTrue(result.contains("key3=value3"));
        assertTrue(result.matches("^(key1=value1,key2=value2,key3=value3|key1=value1,key3=value3,key2=value2|key2=value2,key1=value1,key3=value3|key2=value2,key3=value3,key1=value1|key3=value3,key1=value1,key2=value2|key3=value3,key2=value2,key1=value1)$"));
    }

    @Test
    void encode_shouldHandleEmptyMap() {
        Map<String, String> tags = new HashMap<>();

        String result = QueryTags.encode(tags);

        assertEquals("", result);
    }

    @Test
    void encode_shouldHandleSingleEntry() {
        Map<String, String> tags = new HashMap<>();
        tags.put("key1", "value1");

        String result = QueryTags.encode(tags);

        assertEquals("key1=value1", result);
    }

    @Test
    void encode_shouldHandleSpecialCharacters() {
        Map<String, String> tags = new HashMap<>();
        tags.put("key1", "value1,value2");
        tags.put("key2", "value3,value4");

        String result = QueryTags.encode(tags);

        assertTrue(result.contains("key1=value1,value2"));
        assertTrue(result.contains("key2=value3,value4"));
    }

}