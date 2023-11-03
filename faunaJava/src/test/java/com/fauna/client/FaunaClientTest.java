package com.fauna.client;

import com.fauna.common.configuration.FaunaConfig;
import com.fauna.common.configuration.HttpClientConfig;
import com.fauna.common.connection.Connection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FaunaClientTest {

    @Mock
    private Connection connection;

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

}