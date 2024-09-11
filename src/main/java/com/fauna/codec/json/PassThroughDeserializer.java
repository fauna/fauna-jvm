package com.fauna.codec.json;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class PassThroughDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException, JacksonException {
        // We can improve performance by building this from tokens instead.
        return jp.readValueAsTree().toString();
    }
}
