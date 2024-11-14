package com.fauna.client;

import java.net.http.HttpHeaders;
import java.util.stream.Collectors;

/**
 * A utility class for logging HTTP headers.
 */
public final class Logging {

    private Logging() {
    }

    /**
     * Converts the given HttpHeaders to a string representation.
     *
     * @param headers The HttpHeaders to convert.
     * @return A string representation of the headers.
     */
    public static String headersAsString(final HttpHeaders headers) {
        String hdrs = "NONE";
        if (headers != null) {
            hdrs = headers.map().entrySet().stream().map(
                            entry -> entry.getKey() + ": " + String.join(
                                    ",", entry.getValue()))
                    .collect(Collectors.joining(";"));
        }
        return hdrs;
    }
}
