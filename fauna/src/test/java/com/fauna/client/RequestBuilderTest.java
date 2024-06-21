package com.fauna.client;

import com.fauna.common.configuration.FaunaConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

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
        requestBuilder = new RequestBuilder(faunaConfig);

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
        requestBuilder = new RequestBuilder(faunaConfig);

        HttpRequest httpRequest = requestBuilder.buildRequest("Sample FQL Query");

        assertEquals("true", httpRequest.headers().firstValue(RequestBuilder.Headers.LINEARIZED).get());
        assertEquals("false", httpRequest.headers().firstValue(RequestBuilder.Headers.TYPE_CHECK).get());
        assertNotNull(httpRequest.headers().firstValue(RequestBuilder.Headers.QUERY_TAGS));
        assertEquals("traceParent", httpRequest.headers().firstValue(RequestBuilder.Headers.TRACE_PARENT).get());
    }

    @Test
    @Timeout(value=600, unit = TimeUnit.MILLISECONDS)
    void buildRequest_shouldBeFast() {
        requestBuilder = new RequestBuilder(FaunaConfig.builder().build());

        // Minimizing the amount of work done in .buildRequest(fql) sped this test up from ~600ms to ~300ms on
        // my Intel Mac. - @findgriffin
        IntStream.range(0, 100000).forEach(i -> requestBuilder.buildRequest(String.format("Sample FQL Query %d", i)));
    }
}