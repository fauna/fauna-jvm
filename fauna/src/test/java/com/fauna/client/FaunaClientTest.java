package com.fauna.client;

import com.fauna.common.configuration.FaunaConfig;
import com.fauna.common.configuration.FaunaConfig.FaunaEndpoint;
import com.fauna.exception.QueryCheckException;
import com.fauna.query.builder.Query;
import com.fauna.response.QueryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class FaunaClientTest {

    private FaunaClient defaultClient;
    private FaunaClient localClient;
    @Mock
    public HttpClient mockClient;

    @BeforeEach
    void setUp() {
        defaultClient = new FaunaClient(FaunaConfig.builder().build(), mockClient);
        localClient = new FaunaClient(FaunaConfig.builder().endpoint(FaunaEndpoint.LOCAL).build());
    }

    @Test
    void defaultConfigBuilder() {
        FaunaConfig config = FaunaConfig.builder().build();
        assertEquals("https://db.fauna.com", config.getEndpoint());
        assertEquals("", config.getSecret());
        // The default values are never null.
        assertEquals(Optional.empty(), config.getLinearized());
        assertEquals(Optional.empty(), config.getTypeCheck());
        assertEquals(Optional.empty(), config.getTraceParent());
        assertEquals(new HashMap<>(), config.getQueryTags());
        assertEquals(Duration.ofSeconds(5), config.getQueryTimeout());
    }

    @Test
    void customConfigBuilder() {
        FaunaConfig config = FaunaConfig.builder()
                .endpoint("endpoint")
                .secret("secret")
                .linearized(false)
                .typeCheck(false)
                .traceParent("parent")
                .queryTags(Map.of("t1", "v1", "t2", "v2"))
                .queryTimeout(Duration.ofMinutes(1))
                .build();

        assertEquals("endpoint", config.getEndpoint());
        assertEquals("secret", config.getSecret());
        assertEquals(Optional.of(false), config.getLinearized());
        assertEquals(Optional.of(false), config.getTypeCheck());
        assertEquals(Optional.of("parent"), config.getTraceParent());
        assertEquals(Set.of("t1", "t2"), config.getQueryTags().keySet());
        assertEquals(Duration.ofSeconds(60), config.getQueryTimeout());

    }

    @Test
    void defaltClientConstructor() {
        FaunaClient client = new FaunaClient();
        assertTrue(client.toString().startsWith("com.fauna.client.FaunaClient"));
    }

    @Test
    void customConfigConstructor() {
        FaunaConfig config = FaunaConfig.builder().queryTimeout(Duration.ofSeconds(30)).build();
        FaunaClient client = new FaunaClient(config);
        assertTrue(client.toString().startsWith("com.fauna.client.FaunaClient"));
    }

    @Test
    void customConfigAndClientConstructor() {
        FaunaConfig config = FaunaConfig.builder().queryTimeout(Duration.ofSeconds(10)).build();
        HttpClient multiThreadedClient = HttpClient.newBuilder().executor(Executors.newFixedThreadPool(20))
                .connectTimeout(Duration.ofSeconds(15)).build();
        FaunaClient client = new FaunaClient(config, multiThreadedClient);
        assertTrue(client.toString().startsWith("com.fauna.client.FaunaClient"));
    }

    @Test()
    void environmentVarConfigConstructor() {
        // Note that the secret passed in through the builder is overridden by the FAUNA_* environment variables.
        try (MockedStatic<FaunaConfig.FaunaEnvironment> env = Mockito.mockStatic(FaunaConfig.FaunaEnvironment.class)) {
            env.when(FaunaConfig.FaunaEnvironment::faunaSecret).thenReturn(Optional.of("secret"));
            env.when(FaunaConfig.FaunaEnvironment::faunaEndpoint).thenReturn(Optional.of("endpoint"));
            FaunaConfig faunaConfig = FaunaConfig.builder().secret("overridden").endpoint("overridden").build();
            assertEquals("secret", faunaConfig.getSecret());
            assertEquals("endpoint", faunaConfig.getEndpoint());
        }
    }

    @Test
    void emptyEnvironmentVarConfigConstructor() {
        try (MockedStatic<FaunaConfig.FaunaEnvironment> env = Mockito.mockStatic(FaunaConfig.FaunaEnvironment.class)) {
            env.when(FaunaConfig.FaunaEnvironment::faunaSecret).thenReturn(Optional.empty());
            env.when(FaunaConfig.FaunaEnvironment::faunaEndpoint).thenReturn(Optional.empty());
            FaunaConfig.Builder builder = FaunaConfig.builder().secret("secret").endpoint("endpoint");
            FaunaConfig faunaConf = builder.build();
            assertEquals("secret", faunaConf.getSecret());
            assertEquals("endpoint", faunaConf.getEndpoint());
        }
    }

    @Test
    void nullConfigClientConstructor() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> new FaunaClient(null),
                "null FaunaConfig should throw"
        );
        assertEquals("FaunaConfig cannot be null.", thrown.getMessage() );
    }

    @Test
    void query_WhenFqlIsNull_ShouldThrowIllegalArgumentException() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> defaultClient.query(null),
                "Expected query() to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("The provided FQL query is null."));
    }

    @Test
    void query_WithValidFQL_ShouldCall() throws IOException, InterruptedException {
        HttpResponse resp = mock(HttpResponse.class);
        when(resp.body()).thenReturn("{\"summary\":\"success\"}");
        when(mockClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        QueryResponse response = defaultClient.query(Query.fql("Collection.create({ name: 'Dogs' })"));
        assertEquals("success", response.getSummary());
        assertEquals(0, response.getLastSeenTxn());
        verify(resp, atLeastOnce()).statusCode();
    }

    @Test
    void asyncQuery_WithValidFQL_ShouldCall() throws ExecutionException, InterruptedException {
        HttpResponse resp = mock(HttpResponse.class);
        when(resp.body()).thenReturn("{\"summary\":\"success\"}");
        when(mockClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        CompletableFuture<QueryResponse> future = defaultClient.asyncQuery(Query.fql("Collection.create({ name: 'Dogs' })"));
        QueryResponse response = future.get();
        assertEquals("success", response.getSummary());
        assertEquals(0, response.getLastSeenTxn());
        verify(resp, atLeastOnce()).statusCode();
    }

    @Test
    void query_withFailure_ShouldThrow() throws ExecutionException, InterruptedException {
        HttpResponse resp = mock(HttpResponse.class);
        when(resp.body()).thenReturn("{\"error\":{\"code\":\"invalid_query\"}}");
        when(resp.statusCode()).thenReturn(400);
        when(mockClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        QueryCheckException exc = assertThrows(QueryCheckException.class,
                () -> defaultClient.query(Query.fql("Collection.create({ name: 'Dogs' })")));
        assertEquals("invalid_query", exc.getResponse().getErrorCode());
        assertEquals(400, exc.getResponse().getStatusCode());

    }
    @Test
    void asyncQuery_withFailure_ShouldThrow() throws ExecutionException, InterruptedException {
        HttpResponse resp = mock(HttpResponse.class);
        when(resp.body()).thenReturn("{\"error\":{\"code\":\"invalid_query\"}}");
        when(resp.statusCode()).thenReturn(400);
        when(mockClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        CompletableFuture<QueryResponse> future = defaultClient.asyncQuery(Query.fql("Collection.create({ name: 'Dogs' })"));
        ExecutionException exc = assertThrows(ExecutionException.class, () -> future.get());
        QueryCheckException cause = (QueryCheckException) exc.getCause();
        assertEquals("invalid_query", cause.getResponse().getErrorCode());
        assertEquals(400, cause.getResponse().getStatusCode());

    }

}