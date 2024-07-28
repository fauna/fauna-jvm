package com.fauna.client;

import java.net.http.HttpClient;

public class ScopedFaunaClient extends FaunaClient {
    FaunaClient client;
    FaunaScope scope;


    public ScopedFaunaClient(FaunaClient client, FaunaScope scope) {
        this.client = client;
        this.scope = scope;
    }


    @Override
    RetryStrategy getRetryStrategy() {
        return client.getRetryStrategy();
    }

    @Override
    HttpClient getHttpClient() {
        return client.getHttpClient();
    }

    @Override
    RequestBuilder getRequestBuilder() {
        return client.getRequestBuilder();
    }
}
