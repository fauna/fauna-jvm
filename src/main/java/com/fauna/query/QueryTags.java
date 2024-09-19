package com.fauna.query;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fauna.exception.ClientResponseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class QueryTags extends HashMap<String, String> {
    private static final String EQUALS = "=";
    private static final String COMMA = ",";

    /**
     * Build a set of query tags from a collection of strings.
     * @param tags  1 or more strings of the form "k=v" where k is the tag key, and v is the tag value.
     * @return      A new QueryTags instance.
     */
    public static QueryTags of(String... tags) {
        QueryTags queryTags = new QueryTags();
        for (String tagString : tags) {
            String[] tag = tagString.split(EQUALS);
            if (tag.length == 2) {
                queryTags.put(tag[0].strip(), tag[1].strip());
            } else {
                throw new ClientResponseException("Invalid tag encoding: " + tagString);
            }
        }
        return queryTags;
    }

    public String encode() {
        return this.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(entry -> String.join(EQUALS, entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(COMMA));
    }

    public static QueryTags parse(JsonParser parser) throws IOException {
        if (parser.nextToken() == JsonToken.VALUE_NULL) {
            return null;
        } else if (parser.getCurrentToken() == JsonToken.VALUE_STRING) {
            String tagString = parser.getText();
            if (!tagString.isEmpty()) {
                return QueryTags.of(tagString.split(COMMA));
            } else {
                return new QueryTags();
            }
        } else {
            throw new ClientResponseException("Unexpected token for QueryTags: " + parser.getCurrentToken());
        }
    }
}
