package com.fauna.client;

import com.fauna.beans.Person;
import com.fauna.exception.QueryCheckException;
import com.fauna.exception.ThrottlingException;
import com.fauna.query.QueryOptions;
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
import java.util.Map;
import java.util.Optional;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class FaunaClientTest {

    private FaunaClient defaultClient;
    private QueryOptions defaultOptions = QueryOptions.builder().build();

    @Mock
    public HttpClient mockClient;


    @BeforeEach
    void setUp() {
        defaultClient = new FaunaClient(FaunaConfig.builder().build(), mockClient);
    }

    @Test
    void defaultConfigBuilder() {
        FaunaConfig config = FaunaConfig.builder().build();
        assertEquals("https://db.fauna.com", config.getEndpoint());
        assertEquals("", config.getSecret());
    }

    @Test
    void customConfigBuilder() {
        QueryOptions opts = QueryOptions.builder()
                .linearized(false)
                .typeCheck(false)
                .traceParent("parent")
                .queryTags(Map.of("t1", "v1", "t2", "v2"))
                .timeout(Duration.ofMinutes(1)).build();
        FaunaConfig config = FaunaConfig.builder()
                .endpoint("endpoint")
                .secret("secret")
                .build();

        assertEquals("endpoint", config.getEndpoint());
        assertEquals("secret", config.getSecret());

    }

    @Test
    void defaltClientConstructor() {
        FaunaClient client = new FaunaClient();
        assertTrue(client.toString().startsWith("com.fauna.client.FaunaClient"));
    }

    @Test
    void customConfigConstructor() {
        FaunaConfig config = FaunaConfig.builder().secret("foo").build();
        FaunaClient client = new FaunaClient(config);
        assertTrue(client.toString().startsWith("com.fauna.client.FaunaClient"));
    }

    @Test
    void customConfigAndClientConstructor() {
        QueryOptions opts = QueryOptions.builder().timeout(Duration.ofSeconds(10)).build();
        FaunaConfig config = FaunaConfig.builder().build();
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
            assertEquals("overridden", faunaConfig.getSecret());
            assertEquals("overridden", faunaConfig.getEndpoint());
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
        when(resp.body()).thenReturn("{\"summary\":\"success\",\"stats\":{}}");
        when(mockClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        QueryResponse response = defaultClient.query(Query.fql("Collection.create({ name: 'Dogs' })"));
        assertEquals("success", response.getSummary());
        assertEquals(0, response.getLastSeenTxn());
        verify(resp, atLeastOnce()).statusCode();
    }

    @Test
    void query_WithTypedResponse() throws IOException, InterruptedException {
        HttpResponse resp = mock(HttpResponse.class);
        String baz = "{" +
                "\"firstName\": \"Baz2\"," +
                "\"lastName\": \"Luhrmann2\"," +
                "\"middleInitial\": {\"@int\":\"65\"}," +
                "\"age\": { \"@int\": \"612\" }" +
                "}";
        ;
        String body = "{\"summary\":\"success\",\"stats\":{},\"data\":" + baz + "}";
        when(resp.body()).thenReturn(body);
        when(mockClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        Query fql = Query.fql("Collection.create({ name: 'Dogs' })");
        QueryResponse response = defaultClient.query(fql);
        assertEquals("success", response.getSummary());
        assertEquals(0, response.getLastSeenTxn());
        verify(resp, atLeastOnce()).statusCode();
    }

    @Test
    void asyncQuery_WithValidFQL_ShouldCall() throws ExecutionException, InterruptedException {
        HttpResponse resp = mock(HttpResponse.class);
        when(resp.body()).thenReturn("{\"summary\":\"success\",\"stats\":{}}");
        when(mockClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        CompletableFuture<QueryResponse> future = defaultClient.asyncQuery(Query.fql("Collection.create({ name: 'Dogs' })"));
        QueryResponse response = future.get();
        assertEquals("success", response.getSummary());
        assertEquals(0, response.getLastSeenTxn());
        verify(resp, atLeastOnce()).statusCode();
    }

    @Test
    void query_withFailure_ShouldThrow() {
        HttpResponse resp = mock(HttpResponse.class);
        when(resp.body()).thenReturn("{\"stats\":{},\"error\":{\"code\":\"invalid_query\"}}");
        when(resp.statusCode()).thenReturn(400);
        when(mockClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        QueryCheckException exc = assertThrows(QueryCheckException.class,
                () -> defaultClient.query(Query.fql("Collection.create({ name: 'Dogs' })")));
        assertEquals("invalid_query", exc.getResponse().getErrorCode());
        assertEquals(400, exc.getResponse().getStatusCode());

    }

    @Test
    void asyncQuery_withFailure_ShouldThrow() {
        HttpResponse resp = mock(HttpResponse.class);
        when(resp.body()).thenReturn("{\"stats\":{},\"error\":{\"code\":\"invalid_query\"}}");
        when(resp.statusCode()).thenReturn(400);
        when(mockClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        CompletableFuture<QueryResponse> future = defaultClient.asyncQuery(Query.fql("Collection.create({ name: 'Dogs' })"));
        ExecutionException exc = assertThrows(ExecutionException.class, () -> future.get());
        QueryCheckException cause = (QueryCheckException) exc.getCause();
        assertEquals("invalid_query", cause.getResponse().getErrorCode());
        assertEquals(400, cause.getResponse().getStatusCode());

    }

    @Test
    void asyncQuery_withNoRetries_ShouldNotRetry() {
        HttpResponse resp = mock(HttpResponse.class);
        when(resp.body()).thenReturn("{\"stats\":{},\"error\":{\"code\":\"limit_exceeded\"}}");
        when(resp.statusCode()).thenReturn(429);
        when(mockClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        CompletableFuture<QueryResponse> future = defaultClient.asyncQuery(Query.fql("Collection.create({ name: 'Dogs' })"), defaultOptions, FaunaClient.NO_RETRY_STRATEGY);
        ExecutionException exc = assertThrows(ExecutionException.class, () -> future.get());
        ThrottlingException cause = (ThrottlingException) exc.getCause();
        assertEquals("limit_exceeded", cause.getResponse().getErrorCode());
        assertEquals(429, cause.getResponse().getStatusCode());
        verify(mockClient, times(1)).sendAsync(any(), any());
    }

    @Test
    void asyncQuery_withRetryableException_ShouldRetry() {
        // GIVEN
        HttpResponse resp = mock(HttpResponse.class);
        int retryAttempts = 10;
        RetryStrategy fastRetry = new ExponentialBackoffStrategy(retryAttempts,
                1f, 10, 10, 0.1f);
        when(resp.body()).thenReturn("{\"stats\":{},\"error\":{\"code\":\"limit_exceeded\"}}");
        when(resp.statusCode()).thenReturn(429);
        when(mockClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        // WHEN
        CompletableFuture<QueryResponse> future = defaultClient.asyncQuery(
                Query.fql("Collection.create({ name: 'Dogs' })"),
                QueryOptions.builder().build(), fastRetry);
        // THEN
        ExecutionException exc = assertThrows(ExecutionException.class, () -> future.get());
        ThrottlingException cause = (ThrottlingException) exc.getCause();
        assertEquals("limit_exceeded", cause.getResponse().getErrorCode());
        assertEquals(429, cause.getResponse().getStatusCode());
        verify(mockClient, times(retryAttempts + 1)).sendAsync(any(), any());
    }

    @Test
    void asyncQuery_shouldSucceedOnRetry() throws ExecutionException, InterruptedException {
        // GIVEN
        RetryStrategy fastRetry = new ExponentialBackoffStrategy(3,
                1f, 10, 10, 0.1f);
        HttpResponse retryableResp = mock(HttpResponse.class);
        when(retryableResp.body()).thenReturn("{\"stats\":{},\"error\":{\"code\":\"limit_exceeded\"}}");
        when(retryableResp.statusCode()).thenReturn(429);

        HttpResponse successResp = mock(HttpResponse.class);
        when(successResp.body()).thenReturn("{\"stats\": {}}");
        when(successResp.statusCode()).thenReturn(200);
        when(mockClient.sendAsync(any(), any())).thenReturn(
                CompletableFuture.supplyAsync(() -> retryableResp), CompletableFuture.supplyAsync(() -> successResp));

        // WHEN
        CompletableFuture<QueryResponse> future = defaultClient.asyncQuery(
                Query.fql("Collection.create({ name: 'Dogs' })"),
                QueryOptions.builder().build(), fastRetry);
        // THEN
        QueryResponse success = future.get();
        assertEquals("", success.getSummary());
        verify(mockClient, times(2)).sendAsync(any(), any());
    }

}