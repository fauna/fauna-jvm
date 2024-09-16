package com.fauna.response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QueryTags extends HashMap<String, String> {
    public static QueryTags of(String... strings) {
        QueryTags tags = new QueryTags();
        if (strings.length >= 2 && strings.length % 2 == 0) {
            for (int i = 0; i < strings.length; i += 2) {
                tags.put(strings[i], strings[i + 1]);
            }
        }
        return tags;
    }

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
