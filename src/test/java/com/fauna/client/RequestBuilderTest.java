package com.fauna.client;

import com.fauna.query.QueryOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

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
        assertTrue(httpRequest.bodyPublisher().get().contentLength() > 0);
        assertNotNull(httpRequest.headers().firstValue(RequestBuilder.Headers.AUTHORIZATION));
        assertEquals("Bearer secret", httpRequest.headers().firstValue(RequestBuilder.Headers.AUTHORIZATION).get());
    }

    @Test
    void buildRequest_shouldIncludeOptionalHeadersWhenPresent() {
        QueryOptions options = QueryOptions.builder().timeout(Duration.ofSeconds(15))
                .linearized(true).typeCheck(true).traceParent("traceParent").build();

        HttpRequest httpRequest = requestBuilder.buildRequest(fql("Sample FQL Query"), options);

        assertEquals("true", httpRequest.headers().firstValue(RequestBuilder.Headers.LINEARIZED).get());
        assertEquals("true", httpRequest.headers().firstValue(RequestBuilder.Headers.TYPE_CHECK).get());
        assertNotNull(httpRequest.headers().firstValue(RequestBuilder.Headers.QUERY_TAGS));
        assertEquals("traceParent", httpRequest.headers().firstValue(RequestBuilder.Headers.TRACE_PARENT).get());
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