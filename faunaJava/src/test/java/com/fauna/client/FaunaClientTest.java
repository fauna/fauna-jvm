package com.fauna.client;

import com.fauna.common.configuration.FaunaConfig;
import com.fauna.common.configuration.HttpClientConfig;
import com.fauna.common.connection.Connection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class FaunaClientTest {

    @Mock
    private Connection connection;

    @Mock
    private HttpResponse<String> httpResponse;

    private FaunaClient faunaClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        FaunaConfig faunaConfig = FaunaConfig.builder().build();
        HttpClientConfig httpClientConfig = HttpClientConfig.builder().build();
        faunaClient = new FaunaClient(faunaConfig, httpClientConfig, connection);
    }

    @Test
    void query_WhenFqlIsNull_ShouldThrowIllegalArgumentException() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> faunaClient.query(null),
                "Expected query() to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("The provided FQL query is null."));
    }

    @Test
    void query_WhenFqlIsValid_ShouldReturnHttpResponse() {
        String fql = "valid FQL query";
        when(connection.performRequest(fql)).thenReturn(CompletableFuture.completedFuture(httpResponse));
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("{\"data\": {}}");

        CompletableFuture<HttpResponse<String>> result = faunaClient.query(fql);

        assertNotNull(result);
        assertTrue(result.isDone());
        assertEquals(httpResponse, result.join());
    }

}