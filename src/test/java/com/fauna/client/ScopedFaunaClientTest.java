package com.fauna.client;

import com.fauna.query.builder.Query;
import com.fauna.response.QueryResponse;
import com.fauna.response.QuerySuccess;
import com.fauna.types.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.fauna.client.FaunaScope.BuiltIn.SERVER_READ_ONLY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ScopedFaunaClientTest {

    private FaunaClient scopedClient;

    @Mock
    public HttpClient mockHttpClient;


    @BeforeEach
    void setUp() {
        FaunaClient baseClient = new BaseFaunaClient(FaunaConfig.LOCAL, mockHttpClient, FaunaClient.NO_RETRY_STRATEGY);
        scopedClient = Fauna.scoped(baseClient, "myDB", SERVER_READ_ONLY);
    }


    @Test
    void query_shouldHaveScopedAuthHeader() throws IOException, InterruptedException {
        HttpResponse resp = mock(HttpResponse.class);
        when(resp.body()).thenReturn("{\"summary\":\"success\",\"stats\":{}}");
        ArgumentMatcher<HttpRequest> matcher = new HttpRequestMatcher(Map.of("Authorization", "secret:myDB:@role/server-readonly"));

        when(mockHttpClient.sendAsync(argThat(matcher), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        QuerySuccess<Document> response = scopedClient.query(Query.fql("Collection.create({ name: 'Dogs' })"), Document.class);
        assertEquals("success", response.getSummary());
        assertEquals(0, response.getLastSeenTxn());
        verify(resp, atLeastOnce()).statusCode();
    }

    @Test
    void asyncQuery_shouldHaveScopedAuthHeader() throws InterruptedException, ExecutionException {
        HttpResponse resp = mock(HttpResponse.class);
        when(resp.body()).thenReturn("{\"summary\":\"success\",\"stats\":{}}");
        ArgumentMatcher<HttpRequest> matcher = new HttpRequestMatcher(Map.of("Authorization", "secret:myDB:@role/server-readonly"));
        when(mockHttpClient.sendAsync(argThat(matcher), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        CompletableFuture<QuerySuccess<Document>> future = scopedClient.asyncQuery(Query.fql("Collection.create({ name: 'Dogs' })"), Document.class);
        QueryResponse response = future.get();
        assertEquals("success", response.getSummary());
        assertEquals(0, response.getLastSeenTxn());
        verify(resp, atLeastOnce()).statusCode();
    }

    @Test
    void recursiveScopedClient_shouldhaveCorrectHeader() {
        // Default role is "server"
        FaunaClient recursive = Fauna.scoped(scopedClient, "myOtherDB");
        HttpResponse resp = mock(HttpResponse.class);
        when(resp.body()).thenReturn("{\"summary\":\"success\",\"stats\":{}}");
        ArgumentMatcher<HttpRequest> matcher = new HttpRequestMatcher(Map.of("Authorization", "secret:myOtherDB:@role/server"));

        when(mockHttpClient.sendAsync(argThat(matcher), any())).thenReturn(CompletableFuture.supplyAsync(() -> resp));
        recursive.query(Query.fql("Collection.create({ name: 'Dogs' })"), Document.class);
    }
}
