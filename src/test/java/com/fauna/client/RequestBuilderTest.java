package com.fauna.client;

import com.fauna.query.QueryOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.fauna.client.RequestBuilder.Headers.AUTHORIZATION;
import static com.fauna.client.RequestBuilder.Headers.DRIVER_ENV;
import static com.fauna.client.RequestBuilder.Headers.LINEARIZED;
import static com.fauna.client.RequestBuilder.Headers.QUERY_TAGS;
import static com.fauna.client.RequestBuilder.Headers.TRACE_PARENT;
import static com.fauna.client.RequestBuilder.Headers.TYPE_CHECK;
import static com.fauna.query.builder.Query.fql;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestBuilderTest {

    private final FaunaConfig faunaConfig = FaunaConfig.builder()
                .endpoint(FaunaConfig.FaunaEndpoint.LOCAL)
                .secret("secret").build();;

    private final RequestBuilder requestBuilder = RequestBuilder.queryRequestBuilder(faunaConfig);


    @Test
    void buildRequest_shouldConstructCorrectHttpRequest() {
        HttpRequest httpRequest = requestBuilder.buildRequest(fql("Sample fql query"), null);

        assertEquals("http://localhost:8443/query/1", httpRequest.uri().toString());
        assertEquals("POST", httpRequest.method());
        assertTrue(httpRequest.bodyPublisher().orElseThrow().contentLength() > 0);
        HttpHeaders headers = httpRequest.headers();
        assertTrue(headers.firstValue(DRIVER_ENV).orElse("").contains("driver=java"));
        assertNotNull(headers.firstValue(AUTHORIZATION));
        assertEquals("Bearer secret", headers.firstValue(AUTHORIZATION).orElseThrow());
    }

    @Test
    void buildRequest_shouldIncludeOptionalHeadersWhenPresent() {
        QueryOptions options = QueryOptions.builder().timeout(Duration.ofSeconds(15))
                .linearized(true).typeCheck(true).traceParent("traceParent").build();

        HttpRequest httpRequest = requestBuilder.buildRequest(fql("Sample FQL Query"), options);
        HttpHeaders headers = httpRequest.headers();

        assertEquals("true", headers.firstValue(LINEARIZED).orElseThrow());
        assertEquals("true", headers.firstValue(TYPE_CHECK).orElseThrow());
        assertNotNull(headers.firstValue(QUERY_TAGS));
        assertEquals("traceParent", headers.firstValue(TRACE_PARENT).orElseThrow());
    }

    @Test
    @Timeout(value=1000, unit = TimeUnit.MILLISECONDS)
    void buildRequest_shouldBeFast() {
        // This was faster, but now I think it's taking time to do things like create the FaunaRequest object.
        // Being able to build 10k requests per second still seems like reasonable performance.
        IntStream.range(0, 10000).forEach(i -> requestBuilder.buildRequest(
                fql("Sample FQL Query ${i}", Map.of("i", i)), null));
    }
}