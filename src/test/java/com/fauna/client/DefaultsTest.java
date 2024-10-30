package com.fauna.client;

import com.fauna.codec.DefaultCodecProvider;
import com.fauna.event.EventSource;
import com.fauna.event.FeedOptions;
import com.fauna.event.StreamOptions;
import com.fauna.query.QueryOptions;
import com.fauna.event.StreamRequest;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.logging.Level;

import static com.fauna.query.builder.Query.fql;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultsTest {
    public static FaunaClient client = Fauna.client();
    public static FaunaClient local = Fauna.local();
    public static QueryOptions options = QueryOptions.builder().build();
    public static RequestBuilder queryRequestBuilder = client.getRequestBuilder();
    public static RequestBuilder streamRequestBuilder = client.getStreamRequestBuilder();
    public static RequestBuilder feedRequestBuilder = client.getFeedRequestBuilder();
    private static EventSource source = EventSource.fromToken("token");

    @Test
    public void testClientDefaults() {
        assertTrue(client.getHttpClient().connectTimeout().isEmpty());
        assertTrue(client.getFaunaSecret().isEmpty());
        assertTrue(client.getLastTransactionTs().isEmpty());
        assertEquals(FaunaClient.DEFAULT_RETRY_STRATEGY, client.getRetryStrategy());
        assertEquals(Level.WARNING, client.getLogger().getLevel());
    }

    @Test
    public void testLocalClientDefaults() {
        assertTrue(local.getHttpClient().connectTimeout().isEmpty());
        assertEquals("secret", local.getFaunaSecret());
        assertTrue(local.getLastTransactionTs().isEmpty());
        assertEquals(FaunaClient.DEFAULT_RETRY_STRATEGY, local.getRetryStrategy());
        assertEquals(Level.WARNING, local.getLogger().getLevel());
    }

    @Test
    public void testTimeoutDefaults() {
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create("https://hello.world.com")).build();
        // Java does not set the HTTP request timeout by default.
        assertTrue(httpRequest.timeout().isEmpty());

        // Default Query timeout is 5 seconds.
        assertEquals(5000, options.getTimeoutMillis().orElseThrow());

        HttpRequest queryRequest = feedRequestBuilder.buildRequest(fql(""),
                QueryOptions.builder().build(), DefaultCodecProvider.SINGLETON, 0L);
        // Default HTTP timeout (for queries) is 5+5=10s.
        assertEquals(Duration.ofSeconds(10), queryRequest.timeout().orElseThrow());
        assertEquals("5000", queryRequest.headers().firstValue(RequestBuilder.Headers.QUERY_TIMEOUT_MS).orElseThrow());

        // Feeds follow the same defaults as queries.
        HttpRequest feedRequest = feedRequestBuilder.buildFeedRequest(source, FeedOptions.DEFAULT);
        assertEquals("5000", feedRequest.headers().firstValue(RequestBuilder.Headers.QUERY_TIMEOUT_MS).orElseThrow());
        assertEquals(Duration.ofSeconds(10), feedRequest.timeout().orElseThrow());

        // We do not want a timeout set for stream requests, because the client may hold the stream open indefinitely.
        HttpRequest streamRequest = streamRequestBuilder.buildStreamRequest(source, StreamOptions.builder().build());
        assertTrue(streamRequest.headers().firstValue(RequestBuilder.Headers.QUERY_TIMEOUT_MS).isEmpty());
        assertTrue(streamRequest.timeout().isEmpty());

    }

    @Test
    public void testNullQueryTimeouts() {
        // Show that it's possible to prevent the HTTP client timeout from being set
        QueryOptions override = QueryOptions.builder().timeout(null).build();
        HttpRequest queryRequest = queryRequestBuilder.buildRequest(fql(""), override, DefaultCodecProvider.SINGLETON, 0L);
        assertTrue(queryRequest.timeout().isEmpty());
        assertTrue(queryRequest.headers().firstValue(RequestBuilder.Headers.QUERY_TIMEOUT_MS).isEmpty());

        HttpRequest feedRequest = feedRequestBuilder.buildFeedRequest(source, FeedOptions.builder().timeout(null).build());
        assertTrue(feedRequest.timeout().isEmpty());
        assertTrue(feedRequest.headers().firstValue(RequestBuilder.Headers.QUERY_TIMEOUT_MS).isEmpty());
    }

    @Test
    public void testOverridingTimeouts() {
        HttpRequest streamRequest = streamRequestBuilder.buildStreamRequest(source, StreamOptions.builder().timeout(Duration.ofMinutes(10)).build());
        assertEquals(Duration.ofMinutes(10), streamRequest.timeout().orElseThrow());
    }

    @Test
    public void testFeedDefaults() {
        RequestBuilder builder = Fauna.client().getFeedRequestBuilder();
        HttpRequest req = builder.buildFeedRequest(source, FeedOptions.DEFAULT);
        assertEquals(Duration.ofSeconds(10), req.timeout().orElseThrow()); // Unlike query, the default timeout for feeds is not set.
        assertEquals("POST", req.method());
        assertEquals("https://db.fauna.com/feed/1", req.uri().toString());
    }


}
