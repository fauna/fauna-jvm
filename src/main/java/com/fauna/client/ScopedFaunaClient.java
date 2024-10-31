package com.fauna.client;

import java.net.http.HttpClient;

public class ScopedFaunaClient extends FaunaClient {
    private final FaunaClient client;
    private final RequestBuilder requestBuilder;
    private final RequestBuilder streamRequestBuilder;
    private final RequestBuilder feedRequestBuilder;


    public ScopedFaunaClient(FaunaClient client, FaunaScope scope) {
        super(client.getFaunaSecret(), client.getLogger(), client.getStatsCollector().clone());
        this.client = client;
        this.requestBuilder = client.getRequestBuilder().scopedRequestBuilder(scope.getToken(client.getFaunaSecret()));
        this.streamRequestBuilder = client.getStreamRequestBuilder().scopedRequestBuilder(scope.getToken(client.getFaunaSecret()));
        this.feedRequestBuilder = client.getFeedRequestBuilder().scopedRequestBuilder(scope.getToken(client.getFaunaSecret()));
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
        return requestBuilder;
    }

    @Override
    RequestBuilder getStreamRequestBuilder() {
        return streamRequestBuilder;
    }

    @Override
    RequestBuilder getFeedRequestBuilder() {
        return feedRequestBuilder;
    }
}
