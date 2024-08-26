package com.fauna.codec.json;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class QueryTagsDeserializer extends JsonDeserializer<Map<String, String>> {
    @Override
    public Map<String,String> deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException, JacksonException {
        // We can improve performance by building this from tokens instead.
        switch (jp.currentToken()) {
            case VALUE_NULL:
                return null;
            case VALUE_STRING:
                var raw = jp.getValueAsString();
                Map<String, String> ret = new HashMap<>();
                if (raw.isEmpty()) {
                    return ret;
                }

                String[] tagPairs = jp.getValueAsString().split(",");
                for (String tagPair : tagPairs) {
                    String[] tokens = tagPair.split("=");
                    ret.put(tokens[0], tokens[1]);
                }
                return ret;
            default:
                throw new JsonParseException(jp, MessageFormat.format("Unexpected token `{0}` deserializing query tags", jp.currentToken()));
        }
    }
}
