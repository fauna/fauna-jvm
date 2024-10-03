package com.fauna.client;

import java.net.http.HttpHeaders;
import java.util.stream.Collectors;

public class Logging {
    public static String headersAsString(HttpHeaders headers) {
        String hdrs = "NONE";
        if (headers != null) {
            hdrs = headers.map().entrySet().stream().map(
                entry -> entry.getKey() + ": " + String.join(
                        ",", entry.getValue())).collect(Collectors.joining(";"));
        }
        return hdrs;
    }
}
