package com.fauna.client;

import java.net.http.HttpClient;

public class ScopedFaunaClient extends FaunaClient {
    private final FaunaClient client;
    private final RequestBuilder requestBuilder;
    private final RequestBuilder streamRequestBuilder;


    public ScopedFaunaClient(FaunaClient client, FaunaScope scope) {
        super(client.getFaunaSecret(), client.getLogger());
        this.client = client;
        this.requestBuilder = client.getRequestBuilder().scopedRequestBuilder(scope.getToken(client.getFaunaSecret()));
        this.streamRequestBuilder = client.getStreamRequestBuilder().scopedRequestBuilder(scope.getToken(client.getFaunaSecret()));
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
        return this.requestBuilder;
    }

    @Override
    RequestBuilder getStreamRequestBuilder() {
        return this.streamRequestBuilder;
    }
}
