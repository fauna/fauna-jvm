package com.fauna.codec.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.response.QueryTags;
import com.fauna.response.wire.QueryResponseWire;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QueryTagsParsingTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();


    @Test
    public void deserializeNull() throws IOException {
        JsonParser parser = MAPPER.createParser("null");
        QueryTags tags = QueryTags.parse(parser);
        assertNull(tags);
    }

    @Test
    public void deserializeEmptyString() throws IOException {
        JsonParser parser = MAPPER.createParser("\"\"");
        QueryTags tags = QueryTags.parse(parser);
        assertTrue(tags.isEmpty());
    }

    @Test
    public void deserializeQueryTags() throws IOException {
        JsonParser parser = MAPPER.createParser("\"foo=bar, baz=foo\"");
        QueryTags tags = QueryTags.parse(parser);
        assertTrue(Set.of("foo", "baz").containsAll(tags.keySet()));
        assertEquals("bar", tags.get("foo"));
        assertEquals("foo", tags.get("baz"));
    }
}
