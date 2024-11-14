package com.fauna.client;

import java.net.http.HttpClient;

/**
 * ScopedFaunaClient is a subclass of FaunaClient that applies a scope to the client,
 * limiting the actions and requests to the specified scope.
 */
public final class ScopedFaunaClient extends FaunaClient {
    private final FaunaClient client;
    private final RequestBuilder requestBuilder;
    private final RequestBuilder streamRequestBuilder;
    private final RequestBuilder feedRequestBuilder;

    /**
     * Constructs a new ScopedFaunaClient using the provided FaunaClient and FaunaScope.
     *
     * @param client The FaunaClient instance to base the scoped client on.
     * @param scope The FaunaScope defining the scope for this client.
     */
    public ScopedFaunaClient(final FaunaClient client, final FaunaScope scope) {
        super(client.getFaunaSecret(), client.getLogger(),
                client.getStatsCollector().createNew());
        this.client = client;
        this.requestBuilder = client.getRequestBuilder()
                .scopedRequestBuilder(scope.getToken(client.getFaunaSecret()));
        this.streamRequestBuilder = client.getStreamRequestBuilder()
                .scopedRequestBuilder(scope.getToken(client.getFaunaSecret()));
        this.feedRequestBuilder = client.getFeedRequestBuilder()
                .scopedRequestBuilder(scope.getToken(client.getFaunaSecret()));
    }

    /**
     * Gets the retry strategy for the scoped client.
     *
     * @return The retry strategy used by the client.
     */
    @Override
    public RetryStrategy getRetryStrategy() {
        return client.getRetryStrategy();
    }

    /**
     * Gets the HttpClient used by the scoped client.
     *
     * @return The HttpClient used for making HTTP requests.
     */
    @Override
    public HttpClient getHttpClient() {
        return client.getHttpClient();
    }

    /**
     * Gets the RequestBuilder for the scoped client.
     *
     * @return The RequestBuilder used for constructing HTTP requests.
     */
    @Override
    public RequestBuilder getRequestBuilder() {
        return requestBuilder;
    }

    /**
     * Gets the RequestBuilder for streaming requests.
     *
     * @return The RequestBuilder used for constructing streaming HTTP requests.
     */
    @Override
    public RequestBuilder getStreamRequestBuilder() {
        return streamRequestBuilder;
    }

    /**
     * Gets the RequestBuilder for feed requests.
     *
     * @return The RequestBuilder used for constructing feed HTTP requests.
     */
    @Override
    public RequestBuilder getFeedRequestBuilder() {
        return feedRequestBuilder;
    }
}
