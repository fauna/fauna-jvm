package com.fauna.client;

import com.fauna.exception.ClientException;
import com.fauna.exception.FaunaException;
import com.fauna.query.QueryOptions;
import com.fauna.query.builder.Query;
import com.fauna.response.QueryResponse;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

/**
 * FaunaClient is the main client for interacting with Fauna.
 * It provides functionality to send queries and receive responses.
 */
public class FaunaClient {

    private final HttpClient httpClient;
    private final RequestBuilder requestBuilder;
    private final RetryStrategy retryStrategy;
    /**
     * Construct a new FaunaClient instance with the provided FaunaConfig and HttpClient. This allows
     * the user to have complete control over HTTP Configuration, like timeouts, thread pool size,
     * and so-on.
     *
     * @param faunaConfig The Fauna configuration settings.
     * @param httpClient  A Java HTTP client instance.
     */
    public FaunaClient(FaunaConfig faunaConfig,
                       HttpClient httpClient) {
        this.httpClient = httpClient;
        if (Objects.isNull(faunaConfig)) {
            throw new IllegalArgumentException("FaunaConfig cannot be null.");
        } else if (Objects.isNull(httpClient)) {
            throw new IllegalArgumentException("HttpClient cannot be null.");
        } else {
            this.requestBuilder = new RequestBuilder(faunaConfig);
        }
        this.retryStrategy = ExponentialBackoffStrategy.DEFAULT;
    }

    /**
     * Construct a new FaunaClient instance with the provided FaunaConfig, uses a default HTTP client configuration.
     *
     * @param faunaConfig The Fauna configuration settings.
     */
    public FaunaClient(FaunaConfig faunaConfig) {
        this(faunaConfig, HttpClient.newBuilder().build());
    }

    /**
     * Construct a new FaunaClient instance with default Fauna and HTTP configuration.
     */
    public FaunaClient() {
        this(FaunaConfig.builder().build());
    }

    public static Supplier<CompletableFuture<QueryResponse>> makeAsyncRequest(HttpClient client, HttpRequest request) {
        return () -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(QueryResponse::handleResponse);
    }

    /**
     * Sends a Fauna Query Language (FQL) query to Fauna.
     *
     * @param fql The FQL query to be executed.
     * @return QuerySuccess
     * @throws FaunaException If the provided FQL query is null.
     */
    public CompletableFuture<QueryResponse> asyncQuery(Query fql, QueryOptions options, RetryStrategy strategy) {
        if (Objects.isNull(fql)) {
            throw new IllegalArgumentException("The provided FQL query is null.");
        }
        return new RetryHandler<QueryResponse>(strategy).execute(makeAsyncRequest(
                this.httpClient, requestBuilder.buildRequest(fql, options)));
    }

    public CompletableFuture<QueryResponse> asyncQuery(Query fql, QueryOptions options) {
        return asyncQuery(fql, options, this.retryStrategy);
    }

    /**
     * Sends a Fauna Query Language (FQL) query to Fauna.
     *
     * @param fql The FQL query to be executed.
     * @return QuerySuccess
     * @throws FaunaException If the provided FQL query is null.
     */
    public CompletableFuture<QueryResponse> asyncQuery(Query fql) {
        return asyncQuery(fql, null);
    }

    public QueryResponse query(Query fql, QueryOptions options) throws FaunaException {
        try {
            return this.asyncQuery(fql, options).get();
        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof FaunaException) {
                throw (FaunaException) e.getCause();
            } else {
                throw new ClientException("Unhandled exception.", e);
            }
        }
    }

    public QueryResponse query(Query fql) throws FaunaException {
        return this.query(fql, null);
    }


}

