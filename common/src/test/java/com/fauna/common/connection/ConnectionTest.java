package com.fauna.common.connection;

import com.fauna.common.configuration.FaunaEndpoint;
import com.fauna.common.configuration.FaunaConfig;
import com.fauna.common.configuration.HttpClientConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConnectionTest {

    @Mock
    private FaunaConfig faunaConfig;

    @Mock
    private HttpClientConfig httpClientConfig;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpRequest httpRequest;

    @Mock
    private HttpResponse<String> httpResponse;

    private Connection connection;
    private RequestBuilder spyRequestBuilder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        httpClientConfig = HttpClientConfig.builder()
                .connectTimeout(Duration.ofSeconds(10))
                .maxConnections(5)
                .build();
        faunaConfig = FaunaConfig.builder()
                .endpoint(FaunaEndpoint.LOCAL.toString())
                .secret("secret")
                .build();
        RequestBuilder requestBuilder = RequestBuilder.builder().faunaConfig(faunaConfig).build();
        spyRequestBuilder = spy(requestBuilder);
        connection = new Connection(spyRequestBuilder, httpClientConfig, httpClient);
    }

    @Test
    void performRequest_shouldSendHttpRequestAndReturnResponse() throws Exception {
        String fql = "Sample FQL Query";
        when(spyRequestBuilder.buildRequest(fql)).thenReturn(httpRequest);
        when(httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString()))
                .thenReturn(CompletableFuture.completedFuture(httpResponse));

        CompletableFuture<HttpResponse<String>> futureResponse = connection.performRequest(fql);

        assertTrue(futureResponse.isDone());
        assertEquals(httpResponse, futureResponse.get());
        verify(spyRequestBuilder).buildRequest(fql);
        verify(httpClient).sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

}