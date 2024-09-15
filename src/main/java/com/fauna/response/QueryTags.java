package com.fauna.response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.HashMap;

public class QueryTags extends HashMap<String, String> {
    public static QueryTags parse(JsonParser parser) throws IOException {
        QueryTags tags = new QueryTags();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            String key = parser.nextFieldName();
            String val = parser.nextTextValue();
            tags.put(key, val);
        }
        return tags;
    }
}
