package com.fauna.query;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fauna.exception.ClientResponseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A utility class representing a collection of <a
 * href="https://docs.fauna.com/fauna/current/manage/query-logs/#tags">query tags</a> as a map of key-value pairs.
 * This class extends {@link HashMap} and provides methods to build, encode, and parse query tags.
 */
public class QueryTags extends HashMap<String, String> {
    private static final String EQUALS = "=";
    private static final String COMMA = ",";

    /**
     * Creates a new {@code QueryTags} instance from an array of tag strings.
     * Each tag string must be in the form "key=value".
     *
     * @param tags an array of strings representing tags in the format "k=v",
     *             where {@code k} is the tag key and {@code v} is the tag value.
     * @return a new {@code QueryTags} instance containing the parsed tags.
     * @throws ClientResponseException if a tag string is not in the expected "k=v" format.
     */
    public static QueryTags of(final String... tags) {
        QueryTags queryTags = new QueryTags();
        for (String tagString : tags) {
            String[] tag = tagString.split(EQUALS);
            if (tag.length == 2) {
                queryTags.put(tag[0].strip(), tag[1].strip());
            } else {
                throw new ClientResponseException(
                        "Invalid tag encoding: " + tagString);
            }
        }
        return queryTags;
    }

    /**
     * Encodes the {@code QueryTags} instance as a single string.
     * The tags are sorted by their keys and concatenated in the format "key=value,key=value,...".
     *
     * @return a {@code String} representing the encoded query tags.
     */
    public String encode() {
        return this.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(entry -> String.join(EQUALS, entry.getKey(),
                        entry.getValue()))
                .collect(Collectors.joining(COMMA));
    }

    /**
     * Parses a JSON parser to construct a {@code QueryTags} instance.
     * This method expects the JSON to contain either a null value or a string representation of tags in
     * "key=value,key=value,..." format.
     *
     * @param parser a {@code JsonParser} positioned at the JSON data to parse.
     * @return a {@code QueryTags} instance representing the parsed tags, or {@code null} if the JSON value is null.
     * @throws IOException if an error occurs during parsing.
     * @throws ClientResponseException if the JSON token is not a string or null.
     */
    public static QueryTags parse(final JsonParser parser) throws IOException {
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
            throw new ClientResponseException(
                    "Unexpected token for QueryTags: "
                            + parser.getCurrentToken());
        }
    }
}
