package com.fauna.client;

import java.net.http.HttpClient;
import java.util.Objects;

/**
 * FaunaClient is the main client for interacting with Fauna.
 * It provides functionality to send queries and receive responses.
 */
public final class BaseFaunaClient extends FaunaClient {

    private final HttpClient httpClient;
    private final RequestBuilder baseRequestBuilder;
    private final RequestBuilder streamRequestBuilder;
    private final RequestBuilder feedRequestBuilder;
    private final RetryStrategy retryStrategy;

    /**
     * Construct a new FaunaClient instance with the provided FaunaConfig and HttpClient. This allows
     * the user to have complete control over HTTP Configuration, like timeouts, thread pool size,
     * and so-on.
     *
     * @param faunaConfig   The Fauna configuration settings.
     * @param httpClient    A Java HTTP client instance.
     * @param retryStrategy An implementation of RetryStrategy.
     */
    public BaseFaunaClient(final FaunaConfig faunaConfig,
                           final HttpClient httpClient, final RetryStrategy retryStrategy) {
        super(faunaConfig.getSecret(), faunaConfig.getLogHandler(),
                faunaConfig.getStatsCollector());
        this.httpClient = httpClient;
        if (Objects.isNull(faunaConfig)) {
            throw new IllegalArgumentException("FaunaConfig cannot be null.");
        } else if (Objects.isNull(httpClient)) {
            throw new IllegalArgumentException("HttpClient cannot be null.");
        } else {
            this.baseRequestBuilder =
                    RequestBuilder.queryRequestBuilder(faunaConfig,
                            getLogger());
            this.streamRequestBuilder =
                    RequestBuilder.streamRequestBuilder(faunaConfig,
                            getLogger());
            this.feedRequestBuilder =
                    RequestBuilder.feedRequestBuilder(faunaConfig, getLogger());
        }
        this.retryStrategy = retryStrategy;
    }

    /**
     * Construct a new FaunaClient instance with the provided FaunaConfig, using default HTTP config and retry
     * strategy.
     *
     * @param faunaConfig The Fauna configuration settings.
     */
    public BaseFaunaClient(final FaunaConfig faunaConfig) {
        this(faunaConfig, HttpClient.newBuilder().build(),
                DEFAULT_RETRY_STRATEGY);
    }


    RequestBuilder getRequestBuilder() {
        return this.baseRequestBuilder;
    }

    RequestBuilder getStreamRequestBuilder() {
        return this.streamRequestBuilder;
    }

    RequestBuilder getFeedRequestBuilder() {
        return this.feedRequestBuilder;
    }

    HttpClient getHttpClient() {
        return this.httpClient;
    }

    RetryStrategy getRetryStrategy() {
        return this.retryStrategy;
    }
}
