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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * FaunaClient is the main client for interacting with Fauna.
 * It provides functionality to send queries and receive responses.
 */
public class FaunaClient {

    private final HttpClient httpClient;
    private final RequestBuilder requestBuilder;
    /**
     * Construct a new FaunaClient instance with the provided FaunaConfig and HttpClient. This allows
     * complete control over HTTP Configuration, like timeouts, thread pool size, and so-on.
     *
     *   * Note that FaunaConfig.queryTimeout will be ignored if using this method directly.
     *
     * @param faunaConfig The Fauna configuration settings.
     * @param httpClient  A Java HTTP client instance.
     */
    public FaunaClient(FaunaConfig faunaConfig,
                       HttpClient httpClient) {
        this.httpClient = httpClient;
        if (Objects.isNull(faunaConfig)) {
            throw new IllegalArgumentException("FaunaConfig cannot be null.");
        } else {
            this.requestBuilder = new RequestBuilder(faunaConfig);
        }
        if (Objects.isNull(httpClient)) {
            throw new IllegalArgumentException("HttpClient cannot be null.");
        }
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
     * Construct a new FaunaClient instance with default configuration.
     */
    public FaunaClient() {
        this(FaunaConfig.builder().build());
    }

    /**
     * Sends a Fauna Query Language (FQL) query to Fauna.
     *
     * @param fql The FQL query to be executed.
     * @return QuerySuccess
     * @throws FaunaException If the provided FQL query is null.
     */
    public CompletableFuture<QueryResponse> asyncQuery(
            Query fql, QueryOptions options) {
        if (Objects.isNull(fql)) {
            throw new IllegalArgumentException("The provided FQL query is null.");
        }
        if (Objects.isNull(options)) {
            throw new IllegalArgumentException("The provided queryOptions is null.");
        }
        HttpRequest request = requestBuilder.buildRequest(fql, options);

        return this.httpClient.sendAsync(request,
                HttpResponse.BodyHandlers.ofString()).thenApply(QueryResponse::handleResponse);
    }

    /**
     * Sends a Fauna Query Language (FQL) query to Fauna.
     *
     * @param fql The FQL query to be executed.
     * @return QuerySuccess
     * @throws FaunaException If the provided FQL query is null.
     */
    public CompletableFuture<QueryResponse> asyncQuery(Query fql) {
        if (Objects.isNull(fql)) {
            throw new IllegalArgumentException("The provided FQL query is null.");
        }
        HttpRequest request = requestBuilder.buildRequest(fql, null);
        return this.httpClient.sendAsync(request,
                HttpResponse.BodyHandlers.ofString()).thenApply(QueryResponse::handleResponse);
    }

    public QueryResponse query(Query fql) throws FaunaException {
        try {
            return this.asyncQuery(fql).get();
        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof FaunaException) {
                throw (FaunaException) e.getCause();
            } else {
                throw new ClientException("Unhandled exception.", e);
            }
        }
    }


}

