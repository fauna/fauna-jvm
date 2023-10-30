package com.fauna.common.encoding;

import java.util.Map;
import java.util.stream.Collectors;

public class QueryTags {

    public static String encode(Map<String, String> tags) {
        return tags.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(","));
    }

}
