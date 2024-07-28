package com.fauna.client;

import com.fauna.beans.Person;
import com.fauna.exception.QueryCheckException;
import com.fauna.exception.ThrottlingException;
import com.fauna.query.QueryOptions;
import com.fauna.query.builder.Query;
import com.fauna.response.QueryResponse;
import com.fauna.response.QuerySuccess;
import com.fauna.types.Document;
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

    private FaunaClient client;

    @Mock
    public HttpClient mockHttpClient;


    @BeforeEach
    void setUp() {
        client = Fauna.client(FaunaConfig.DEFAULT, mockHttpClient, FaunaClient.DEFAULT_RETRY_STRATEGY);
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
    void customConfigConstructor() {
        FaunaConfig config = FaunaConfig.builder().secret("foo").build();
        FaunaClient client = Fauna.client(config);
        assertTrue(client.toString().startsWith("com.fauna.client.BaseFaunaClient"));
    }

    @Test
    void customConfigAndClientConstructor() {
        FaunaConfig config = FaunaConfig.builder().build();
        HttpClient multiThreadedClient = HttpClient.newBuilder().executor(Executors.newFixedThreadPool(20))
                .connectTimeout(Duration.ofSeconds(15)).build();
        FaunaClient client = Fauna.client(config, multiThreadedClient, FaunaClient.DEFAULT_RETRY_STRATEGY);
        assertTrue(client.toString().startsWith("com.fauna.client.BaseFaunaClient"));
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
        NullPointerException thrown = assertThrows(
                NullPointerException.class,
                () -> Fauna.client(null),
                "null FaunaConfig should throw"
        );
        assertEquals("FaunaConfig cannot be null.", thrown.getMessage() );
    }


    @Test
    void query_WhenFqlIsNull_ShouldThrowIllegalArgumentException() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> client.query(null, Document.class),
                "Expected query() to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("The provided FQL query is null."));
    }

    @Test
    void query_WithValidFQL_ShouldCall() throws IOException, InterruptedException {
        HttpResponse resp = mock(HttpResponse.class);
        when(resp.body()).thenReturn("{\"summary\":\"success\",\"stats\":{}}");
        when(mockHttpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        QuerySuccess<Document> response = client.query(Query.fql("Collection.create({ name: 'Dogs' })"), Document.class);
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
        when(mockHttpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        Query fql = Query.fql("Collection.create({ name: 'Dogs' })");
        QuerySuccess<Person> response = client.query(fql, Person.class);
        assertEquals("success", response.getSummary());
        assertEquals(0, response.getLastSeenTxn());
        verify(resp, atLeastOnce()).statusCode();
    }

    @Test
    void asyncQuery_WithTypedResponse() throws IOException, InterruptedException, ExecutionException {
        // Given
        HttpResponse resp = mock(HttpResponse.class);
        String baz = "{" +
                "\"firstName\": \"Baz\"," +
                "\"lastName\": \"Luhrmann2\"," +
                "\"middleInitial\": {\"@int\":\"65\"}," +
                "\"age\": { \"@int\": \"612\" }" +
                "}";
        ;
        String body = "{\"summary\":\"success\",\"stats\":{},\"data\":" + baz + "}";
        when(resp.body()).thenReturn(body);
        when(mockHttpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        Query fql = Query.fql("Collection.create({ name: 'Dogs' })");
        // When
        CompletableFuture<QuerySuccess<Person>> future = client.asyncQuery(fql, Person.class);
        QuerySuccess<Person> response = future.get();
        Person data = response.getData();
        // Then
        assertEquals("Baz", data.getFirstName());
        assertEquals("success", response.getSummary());
        assertEquals(0, response.getLastSeenTxn());
        verify(resp, atLeastOnce()).statusCode();
    }

    @Test
    void asyncQuery_WithValidFQL_ShouldCall() throws ExecutionException, InterruptedException {
        HttpResponse resp = mock(HttpResponse.class);
        when(resp.body()).thenReturn("{\"summary\":\"success\",\"stats\":{}}");
        when(mockHttpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        CompletableFuture<QuerySuccess<Document>> future = client.asyncQuery(Query.fql("Collection.create({ name: 'Dogs' })"), Document.class);
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
        when(mockHttpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        QueryCheckException exc = assertThrows(QueryCheckException.class,
                () -> client.query(Query.fql("Collection.create({ name: 'Dogs' })"), Document.class));
        assertEquals("invalid_query", exc.getResponse().getErrorCode());
        assertEquals(400, exc.getResponse().getStatusCode());

    }

    @Test
    void asyncQuery_withFailure_ShouldThrow() {
        HttpResponse resp = mock(HttpResponse.class);
        when(resp.body()).thenReturn("{\"stats\":{},\"error\":{\"code\":\"invalid_query\"}}");
        when(resp.statusCode()).thenReturn(400);
        when(mockHttpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        CompletableFuture<QuerySuccess<Document>> future = client.asyncQuery(Query.fql("Collection.create({ name: 'Dogs' })"), Document.class);
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
        when(mockHttpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        FaunaClient noRetryClient = Fauna.client(FaunaConfig.DEFAULT, mockHttpClient, FaunaClient.NO_RETRY_STRATEGY);
        CompletableFuture<QuerySuccess<Document>> future = noRetryClient.asyncQuery(
                Query.fql("Collection.create({ name: 'Dogs' })"), Document.class);
        ExecutionException exc = assertThrows(ExecutionException.class, () -> future.get());
        ThrottlingException cause = (ThrottlingException) exc.getCause();
        assertEquals("limit_exceeded", cause.getResponse().getErrorCode());
        assertEquals(429, cause.getResponse().getStatusCode());
        verify(mockHttpClient, times(1)).sendAsync(any(), any());
    }

    @Test
    void asyncQuery_withRetryableException_ShouldRetry() {
        // GIVEN
        HttpResponse resp = mock(HttpResponse.class);
        int retryAttempts = 10;
        when(resp.body()).thenReturn("{\"stats\":{},\"error\":{\"code\":\"limit_exceeded\"}}");
        when(resp.statusCode()).thenReturn(429);
        when(mockHttpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        // WHEN

        BaseFaunaClient fastClient = new BaseFaunaClient(FaunaConfig.builder().build(), mockHttpClient,
                new ExponentialBackoffStrategy(retryAttempts, 1f, 10, 10, 0.1f));
        CompletableFuture<QuerySuccess<Document>> future = fastClient.asyncQuery(
                Query.fql("Collection.create({ name: 'Dogs' })"), Document.class);
        // THEN
        ExecutionException exc = assertThrows(ExecutionException.class, () -> future.get());
        ThrottlingException cause = (ThrottlingException) exc.getCause();
        assertEquals("limit_exceeded", cause.getResponse().getErrorCode());
        assertEquals(429, cause.getResponse().getStatusCode());
        verify(mockHttpClient, times(retryAttempts + 1)).sendAsync(any(), any());
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
        when(mockHttpClient.sendAsync(any(), any())).thenReturn(
                CompletableFuture.supplyAsync(() -> retryableResp), CompletableFuture.supplyAsync(() -> successResp));

        // WHEN
        CompletableFuture<QuerySuccess<Document>> future = client.asyncQuery(
                Query.fql("Collection.create({ name: 'Dogs' })"),
                Document.class, QueryOptions.builder().build());
        // THEN
        QuerySuccess<Document> success = future.get();
        assertEquals("", success.getSummary());
        verify(mockHttpClient, times(2)).sendAsync(any(), any());
    }

}