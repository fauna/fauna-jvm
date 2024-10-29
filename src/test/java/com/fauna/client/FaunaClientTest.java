package com.fauna.client;

import com.fauna.beans.Person;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.e2e.beans.Product;
import com.fauna.exception.QueryCheckException;
import com.fauna.exception.ThrottlingException;
import com.fauna.query.QueryOptions;
import com.fauna.query.builder.Query;
import com.fauna.response.QueryResponse;
import com.fauna.response.QuerySuccess;
import com.fauna.types.Document;
import com.fauna.types.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import org.mockito.junit.jupiter.MockitoExtension;

import static com.fauna.query.builder.Query.fql;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



@ExtendWith(MockitoExtension.class)
class FaunaClientTest {

    private FaunaClient client;

    @Mock
    public HttpClient mockHttpClient;

    String productBase = "{\"@doc\":{\"id\":\"406412545672348160\",\"coll\":{\"@mod\":\"Product\"}," +
            "\"ts\":{\"@time\":\"2024-08-16T21:34:16.700Z\"},\"name\":\"%s\",\"quantity\":{\"@int\":\"0\"}}}";
    String bodyBase = "{\"data\":{\"@set\":{\"data\":[%s],\"after\":%s}},\"summary\":\"\"," +
            "\"txn_ts\":1723844145837000,\"stats\":{},\"schema_version\":1723844028490000}";

    @BeforeEach
    void setUp() {
        client = Fauna.client(FaunaConfig.LOCAL, mockHttpClient, FaunaClient.DEFAULT_RETRY_STRATEGY);
    }

    static HttpResponse<InputStream> mockResponse(String body) {
        HttpResponse resp = mock(HttpResponse.class);
        doAnswer(invocationOnMock -> new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8))).when(resp).body();
        return resp;
    }

    @Test
    void defaultClient() {
        FaunaClient client = Fauna.client();
        assertTrue(client.getHttpClient().connectTimeout().isEmpty());
        assertNotNull(client.getStatsCollector());
        assertEquals(URI.create("https://db.fauna.com/query/1"),
                client.getRequestBuilder().buildRequest(
                        fql("hello"), QueryOptions.builder().build(), DefaultCodecProvider.SINGLETON, 1L).uri());
    }

    @Test
    void customConfigBuilder() {
        FaunaConfig config = FaunaConfig.builder()
                .endpoint("endpoint")
                .secret("secret")
                .build();

        assertEquals("endpoint", config.getEndpoint());
        assertEquals("secret", config.getSecret());

    }


    @Test
    void customConfigConstructor() {
        FaunaConfig cfg = FaunaConfig.builder()
                .secret("foo")
                .build();
        FaunaClient client = Fauna.client(cfg);
        assertTrue(client.toString().startsWith("com.fauna.client.BaseFaunaClient"));
        assertNotNull(client.getStatsCollector());
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
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
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
        HttpResponse resp = mockResponse("{\"summary\":\"success\",\"stats\":{}}");
        ArgumentMatcher<HttpRequest> matcher = new HttpRequestMatcher(Map.of("Authorization", "Bearer secret"));
        when(mockHttpClient.sendAsync(argThat(matcher), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        QuerySuccess<Document> response = client.query(fql("Collection.create({ name: 'Dogs' })"), Document.class);
        assertEquals("success", response.getSummary());
        assertNull(response.getLastSeenTxn());
        verify(resp, atLeastOnce()).statusCode();
    }

    @Test
    void query_WithTypedResponse() throws IOException, InterruptedException {
        String baz = "{" +
                "\"firstName\": \"Baz2\"," +
                "\"lastName\": \"Luhrmann2\"," +
                "\"middleInitial\": {\"@int\":\"65\"}," +
                "\"age\": { \"@int\": \"612\" }" +
                "}";
        ;
        String body = "{\"summary\":\"success\",\"stats\":{},\"data\":" + baz + "}";
        HttpResponse resp = mockResponse(body);
        when(mockHttpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        Query fql = fql("Collection.create({ name: 'Dogs' })");
        QuerySuccess<Person> response = client.query(fql, Person.class);
        assertEquals("success", response.getSummary());
        assertNull(response.getLastSeenTxn());
        verify(resp, atLeastOnce()).statusCode();
    }

    @Test
    void asyncQuery_WithTypedResponse() throws IOException, InterruptedException, ExecutionException {
        // Given
        String baz = "{" +
                "\"firstName\": \"Baz\"," +
                "\"lastName\": \"Luhrmann2\"," +
                "\"middleInitial\": {\"@int\":\"65\"}," +
                "\"age\": { \"@int\": \"612\" }" +
                "}";
        ;
        String body = "{\"summary\":\"success\",\"stats\":{},\"data\":" + baz + "}";
        HttpResponse resp = mockResponse(body);
        when(mockHttpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        Query fql = fql("Collection.create({ name: 'Dogs' })");
        // When
        CompletableFuture<QuerySuccess<Person>> future = client.asyncQuery(fql, Person.class);
        QuerySuccess<Person> response = future.get();
        Person data = response.getData();
        // Then
        assertEquals("Baz", data.getFirstName());
        assertEquals("success", response.getSummary());
        assertNull(response.getLastSeenTxn());
        verify(resp, atLeastOnce()).statusCode();
    }

    @Test
    void asyncQuery_WithValidFQL_ShouldCall() throws ExecutionException, InterruptedException {
        HttpResponse resp = mockResponse("{\"summary\":\"success\",\"stats\":{}}");
        when(mockHttpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        CompletableFuture<QuerySuccess<Document>> future = client.asyncQuery(fql("Collection.create({ name: 'Dogs' })"), Document.class);
        QueryResponse response = future.get();
        assertEquals("success", response.getSummary());
        assertNull(response.getLastSeenTxn());
        verify(resp, atLeastOnce()).statusCode();
    }

    @Test
    void query_withFailure_ShouldThrow() {
        HttpResponse resp = mockResponse("{\"stats\":{},\"error\":{\"code\":\"invalid_query\"}}");
        when(resp.statusCode()).thenReturn(400);
        when(mockHttpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        QueryCheckException exc = assertThrows(QueryCheckException.class,
                () -> client.query(fql("Collection.create({ name: 'Dogs' })"), Document.class));
        assertEquals("invalid_query", exc.getResponse().getErrorCode());
        assertEquals(400, exc.getResponse().getStatusCode());

    }

    @Test
    void asyncQuery_withFailure_ShouldThrow() {
        HttpResponse resp = mockResponse("{\"stats\":{},\"error\":{\"code\":\"invalid_query\"}}");
        when(resp.statusCode()).thenReturn(400);
        when(mockHttpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        CompletableFuture<QuerySuccess<Document>> future = client.asyncQuery(fql("Collection.create({ name: 'Dogs' })"), Document.class);
        ExecutionException exc = assertThrows(ExecutionException.class, () -> future.get());
        QueryCheckException cause = (QueryCheckException) exc.getCause();
        assertEquals("invalid_query", cause.getResponse().getErrorCode());
        assertEquals(400, cause.getResponse().getStatusCode());

    }

    @Test
    void asyncQuery_withNoRetries_ShouldNotRetry() {
        HttpResponse resp = mockResponse("{\"stats\":{},\"error\":{\"code\":\"limit_exceeded\"}}");
        when(resp.statusCode()).thenReturn(429);
        when(mockHttpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        FaunaClient noRetryClient = Fauna.client(FaunaConfig.DEFAULT, mockHttpClient, FaunaClient.NO_RETRY_STRATEGY);
        CompletableFuture<QuerySuccess<Document>> future = noRetryClient.asyncQuery(
                fql("Collection.create({ name: 'Dogs' })"), Document.class);
        ExecutionException exc = assertThrows(ExecutionException.class, future::get);
        ThrottlingException cause = (ThrottlingException) exc.getCause();
        assertEquals("limit_exceeded", cause.getResponse().getErrorCode());
        assertEquals(429, cause.getResponse().getStatusCode());
        verify(mockHttpClient, times(1)).sendAsync(any(), any());
    }

    @Test
    void asyncQuery_withRetryableException_ShouldRetry() {
        // GIVEN
        HttpResponse resp = mockResponse("{\"stats\":{},\"error\":{\"code\":\"limit_exceeded\"}}");
        int retryAttempts = 10;
        when(resp.statusCode()).thenReturn(429);
        when(mockHttpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        // WHEN

        BaseFaunaClient fastClient = new BaseFaunaClient(FaunaConfig.builder().build(), mockHttpClient,
                new ExponentialBackoffStrategy(retryAttempts, 1f, 10, 10, 0.1f));
        CompletableFuture<QuerySuccess<Document>> future = fastClient.asyncQuery(
                fql("Collection.create({ name: 'Dogs' })"), Document.class);
        // THEN
        ExecutionException exc = assertThrows(ExecutionException.class, future::get);
        ThrottlingException cause = (ThrottlingException) exc.getCause();
        assertEquals("limit_exceeded", cause.getResponse().getErrorCode());
        assertEquals(429, cause.getResponse().getStatusCode());
        verify(mockHttpClient, times(retryAttempts + 1)).sendAsync(any(), any());
    }

    @Test
    void asyncQuery_shouldSucceedOnRetry() throws ExecutionException, InterruptedException {
        // GIVEN
        HttpResponse retryableResp = mockResponse("{\"stats\":{},\"error\":{\"code\":\"limit_exceeded\"}}");
        when(retryableResp.statusCode()).thenReturn(429);

        HttpResponse successResp = mockResponse("{\"stats\": {}}");
        when(successResp.statusCode()).thenReturn(200);
        when(mockHttpClient.sendAsync(any(), any())).thenReturn(
                CompletableFuture.supplyAsync(() -> retryableResp), CompletableFuture.supplyAsync(() -> successResp));

        // WHEN
        CompletableFuture<QuerySuccess<Document>> future = client.asyncQuery(
                fql("Collection.create({ name: 'Dogs' })"),
                Document.class, QueryOptions.builder().build());
        // THEN
        QuerySuccess<Document> success = future.get();
        assertNull(success.getSummary());
        verify(mockHttpClient, times(2)).sendAsync(any(), any());
    }

    @Test
    void paginateWithQueryOptions() throws IOException {
        QueryOptions options = QueryOptions.builder().timeout(Duration.ofMillis(42)).build();

        HttpResponse firstPageResp = mockResponse(String.format(bodyBase, String.format(productBase, "product-0"), "\"after_token\""));
        when(firstPageResp.statusCode()).thenReturn(200);
        HttpResponse secondPageResp = mockResponse(String.format(bodyBase, String.format(productBase, "product-1"), "null"));
        when(secondPageResp.statusCode()).thenReturn(200);

        ArgumentMatcher<HttpRequest> matcher = new HttpRequestMatcher(Map.of("X-Query-Timeout-Ms", "42"));
        when(mockHttpClient.sendAsync(argThat(matcher), any())).thenReturn(
                CompletableFuture.supplyAsync(() -> firstPageResp),
                CompletableFuture.supplyAsync(() -> secondPageResp));

        PageIterator<Product> iter = client.paginate(fql("Document.all()"), Product.class, options);
        assertTrue(iter.hasNext());
        Page<Product> firstPage = iter.next();
        assertEquals("product-0", firstPage.getData().get(0).getName());
        assertTrue(iter.hasNext());
        Page<Product> secondPage = iter.next();
        assertEquals("product-1", secondPage.getData().get(0).getName());
        assertFalse(iter.hasNext());
    }

    @Test
    void paginateWithQueryOptionsAndNoElementType() {
        QueryOptions options = QueryOptions.builder().timeout(Duration.ofMillis(42)).build();

        HttpResponse firstPageResp = mockResponse(String.format(bodyBase, String.format(productBase, "product-0"), "\"after_token\""));
        when(firstPageResp.statusCode()).thenReturn(200);
        HttpResponse secondPageResp = mockResponse(String.format(bodyBase, String.format(productBase, "product-1"), "null"));
        when(secondPageResp.statusCode()).thenReturn(200);

        ArgumentMatcher<HttpRequest> matcher = new HttpRequestMatcher(Map.of("X-Query-Timeout-Ms", "42"));
        when(mockHttpClient.sendAsync(argThat(matcher), any())).thenReturn(
                CompletableFuture.supplyAsync(() -> firstPageResp),
                CompletableFuture.supplyAsync(() -> secondPageResp));

        PageIterator<Object> iter = client.paginate(fql("Document.all()"), options);
        assertTrue(iter.hasNext());
        Page<Object> firstPage = iter.next();
        assertEquals("product-0", ((Document) firstPage.getData().get(0)).get("name"));
        assertTrue(iter.hasNext());
        Page<Object> secondPage = iter.next();
        assertEquals("product-1", ((Document) secondPage.getData().get(0)).get("name"));
        assertFalse(iter.hasNext());
    }

}