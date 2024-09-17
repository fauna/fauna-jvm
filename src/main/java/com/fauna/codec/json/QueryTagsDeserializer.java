package com.fauna.codec.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class QueryTagsDeserializer extends JsonDeserializer<Map<String, String>> {
    @Override
    public Map<String,String> deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException {
        // We can improve performance by building this from tokens instead.
        switch (jp.currentToken()) {
            case VALUE_NULL:
                return null;
            case VALUE_STRING:
                var rawString = jp.getValueAsString();

                if (rawString.isEmpty()) {
                    return Map.of();
                }

                return Arrays.stream(rawString.split(","))
                        .map(queryTag -> queryTag.split("="))
                        .filter(parts -> parts.length >= 2)
                        .collect(Collectors.toMap(t -> t[0], t -> t[1]));
            default:
                throw new JsonParseException(jp, MessageFormat.format("Unexpected token `{0}` deserializing query tags", jp.currentToken()));
        }
    }
}
