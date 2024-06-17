package com.fauna.common.connection;

import com.fauna.common.configuration.FaunaConfig;
import org.junit.jupiter.api.Test;

import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestBuilderTest {

    private FaunaConfig faunaConfig;

    private RequestBuilder requestBuilder;

    @Test
    void buildRequest_shouldConstructCorrectHttpRequest() {
        String fql = "Sample FQL Query";

        faunaConfig = FaunaConfig.builder()
                .endpoint("http://localhost:8443")
                .secret("secret")
                .queryTimeout(Duration.ofSeconds(5))
                .build();
        requestBuilder = RequestBuilder.builder().faunaConfig(faunaConfig).build();

        HttpRequest httpRequest = requestBuilder.buildRequest(fql);

        assertEquals("http://localhost:8443", httpRequest.uri().toString());
        assertEquals("POST", httpRequest.method());
        assertTrue(httpRequest.bodyPublisher().get().contentLength() > 0);
        assertNotNull(httpRequest.headers().firstValue(RequestBuilder.Headers.AUTHORIZATION));
        assertEquals("Bearer secret", httpRequest.headers().firstValue(RequestBuilder.Headers.AUTHORIZATION).get());
    }

    @Test
    void buildRequest_shouldIncludeOptionalHeadersWhenPresent() {
        Map<String, String> queryTags = new HashMap<>();
        queryTags.put("tag1", "value1");
        queryTags.put("tag2", "value2");

        faunaConfig = FaunaConfig.builder()
                .endpoint("http://localhost:8443")
                .secret("secret")
                .queryTimeout(Duration.ofSeconds(5))
                .linearized(true)
                .typeCheck(false)
                .queryTags(queryTags)
                .traceParent("traceParent")
                .build();
        requestBuilder = RequestBuilder.builder().faunaConfig(faunaConfig).build();

        HttpRequest httpRequest = requestBuilder.buildRequest("Sample FQL Query");

        assertEquals("true", httpRequest.headers().firstValue(RequestBuilder.Headers.LINEARIZED).get());
        assertEquals("false", httpRequest.headers().firstValue(RequestBuilder.Headers.TYPE_CHECK).get());
        assertNotNull(httpRequest.headers().firstValue(RequestBuilder.Headers.QUERY_TAGS));
        assertEquals("traceParent", httpRequest.headers().firstValue(RequestBuilder.Headers.TRACE_PARENT).get());
    }
}