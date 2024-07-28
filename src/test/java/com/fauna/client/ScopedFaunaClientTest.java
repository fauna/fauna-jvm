package com.fauna.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;

import static com.fauna.client.FaunaScope.BuiltIn.SERVER_READ_ONLY;

@ExtendWith(MockitoExtension.class)
public class ScopedFaunaClientTest {

    private BaseFaunaClient client;

    @Mock
    public HttpClient mockHttpClient;


    @BeforeEach
    void setUp() {
        client = new BaseFaunaClient(FaunaConfig.DEFAULT, mockHttpClient, FaunaClient.DEFAULT_RETRY_STRATEGY);
    }

    @Test
    void buildScopedClient() {
        Fauna.scoped(client, "myDB");
        Fauna.scoped(client, "myDB", SERVER_READ_ONLY);
    }
}
