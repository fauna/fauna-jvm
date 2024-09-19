package com.fauna.client;

import com.fauna.codec.CodecProvider;
import com.fauna.codec.CodecRegistry;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.DefaultCodecRegistry;
import com.fauna.query.QueryOptions;
import com.fauna.stream.StreamRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.fauna.client.RequestBuilder.Headers.AUTHORIZATION;
import static com.fauna.client.RequestBuilder.Headers.DRIVER_ENV;
import static com.fauna.client.RequestBuilder.Headers.LINEARIZED;
import static com.fauna.client.RequestBuilder.Headers.MAX_CONTENTION_RETRIES;
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
            .secret("secret").build();

    private final RequestBuilder requestBuilder = RequestBuilder.queryRequestBuilder(faunaConfig);

    private final CodecRegistry codecRegistry = new DefaultCodecRegistry();
    private final CodecProvider codecProvider = new DefaultCodecProvider(codecRegistry);


    @Test
    void buildRequest_shouldConstructCorrectHttpRequest() {
        HttpRequest httpRequest = requestBuilder.buildRequest(fql("Sample fql query"), null, codecProvider);

        assertEquals("http://localhost:8443/query/1", httpRequest.uri().toString());
        assertEquals("POST", httpRequest.method());
        assertTrue(httpRequest.bodyPublisher().orElseThrow().contentLength() > 0);
        HttpHeaders headers = httpRequest.headers();
        assertTrue(headers.firstValue(DRIVER_ENV).orElse("").contains("runtime=java"));
        assertTrue(headers.firstValue(DRIVER_ENV).orElse("").contains("driver="));
        assertNotNull(headers.firstValue(AUTHORIZATION));
        assertEquals("Bearer secret", headers.firstValue(AUTHORIZATION).orElseThrow());
        assertEquals("3", headers.firstValue(MAX_CONTENTION_RETRIES).orElseThrow());
    }

    @Test
    void buildRequest_shouldIncludeOptionalHeadersWhenPresent() {
        QueryOptions options = QueryOptions.builder().timeout(Duration.ofSeconds(15))
                .linearized(true).typeCheck(true).traceParent("traceParent").build();

        HttpRequest httpRequest = requestBuilder.buildRequest(fql("Sample FQL Query"), options, codecProvider);
        HttpHeaders headers = httpRequest.headers();

        assertEquals("true", headers.firstValue(LINEARIZED).orElseThrow());
        assertEquals("true", headers.firstValue(TYPE_CHECK).orElseThrow());
        assertNotNull(headers.firstValue(QUERY_TAGS));
        assertEquals("traceParent", headers.firstValue(TRACE_PARENT).orElseThrow());
    }

    @Test
    void buildStreamRequestBody_shouldOnlyIncludeToken() throws IOException {
        // Given
        StreamRequest request = new StreamRequest("tkn");
        // When
        String body = requestBuilder.buildStreamRequestBody(request);
        // Then
        assertEquals("{\"token\":\"tkn\"}", body);
    }

    @Test
    void buildStreamRequestBody_shouldIncludeCursor() throws IOException {
        // Given
        StreamRequest request = new StreamRequest("tkn", "cur");
        // When
        String body = requestBuilder.buildStreamRequestBody(request);
        // Then
        assertEquals("{\"token\":\"tkn\",\"cursor\":\"cur\"}", body);
    }

    @Test
    void buildStreamRequestBody_shouldIncludeTimestamp() throws IOException {
        // Given
        StreamRequest request = new StreamRequest("tkn", Long.MAX_VALUE / 2);
        // When
        String body = requestBuilder.buildStreamRequestBody(request);
        // Then
        assertEquals("{\"token\":\"tkn\",\"start_ts\":4611686018427387903}", body);
    }

    @Test
    @Timeout(value=1_000, unit = TimeUnit.MILLISECONDS)
    void buildRequest_shouldBeFast() {
        // This was faster, but now I think it's taking time to do things like create the FaunaRequest object.
        // Being able to build 10k requests per second still seems like reasonable performance.
        IntStream.range(0, 10_000).forEach(i -> requestBuilder.buildRequest(
                fql("Sample FQL Query ${i}", Map.of("i", i)), null, codecProvider));
    }

    @Test
    @Timeout(value=1000, unit = TimeUnit.MILLISECONDS)
    void buildRequest_withQueryOptions_shouldBeFast() {
        // I tried implementing a cache in RequestBuilder.java that re-uses the HttpRequest.Builder if the QueryOptions
        // are identical to a previous request. It wasn't any faster, even for up to 1MM buildRequest iterations. I'm
        // leaving this test here in case we ever want to prove that requestBuilder is "fast enough".
        // It takes 90ms on my Mac to build 1k requests. That seems "good enough" for now!
        List<QueryOptions> opts = IntStream.range(0, 100).mapToObj(i -> QueryOptions.builder()
                .queryTag("key" + i, String.valueOf(i))
                .timeout(Duration.ofSeconds(i % 10 ))
                .traceParent("trace" + i)
                .typeCheck(i % 2 == 0)
                .linearized((i+1) % 2 == 0)
                .build()).collect(Collectors.toList());
        IntStream.range(0, 1_000).forEach(i -> requestBuilder.buildRequest(
                fql("Sample FQL Query ${i}", Map.of("i", i)), opts.get(i % 100), codecProvider));
    }
}