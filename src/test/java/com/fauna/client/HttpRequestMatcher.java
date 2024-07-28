package com.fauna.client;

import org.mockito.ArgumentMatcher;

import java.net.http.HttpRequest;
import java.util.List;
import java.util.Map;

public class HttpRequestMatcher implements ArgumentMatcher<HttpRequest> {
    private final Map<String, String> expectedHeaders;


    public HttpRequestMatcher(Map<String, String> expectedHeaders) {
        this.expectedHeaders = expectedHeaders;
    }

    @Override
    public boolean matches(HttpRequest httpRequest) {
        Map<String, List<String>> headers = httpRequest.headers().map();
        for (Map.Entry<String, String> header : expectedHeaders.entrySet()) {
            // It's possible to have multiple headers returned, but assert that we only get one.
            if (!headers.getOrDefault(header.getKey(), List.of()).equals(List.of(header.getValue()))) {
                return false;
            }
        }
        return true;
    }

}
