package com.fauna.client;

import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;
import com.fauna.exception.ClientException;
import com.fauna.exception.FaunaException;
import com.fauna.mapping.MappingContext;
import com.fauna.query.QueryOptions;
import com.fauna.query.builder.Query;
import com.fauna.response.QueryResponse;
import com.fauna.response.QuerySuccess;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

/**
 * FaunaClient is the main client for interacting with Fauna.
 * It provides functionality to send queries and receive responses.
 */
public class FaunaClient {

    public static final RetryStrategy DEFAULT_RETRY_STRATEGY = ExponentialBackoffStrategy.builder().build();
    public static final RetryStrategy NO_RETRY_STRATEGY = new NoRetryStrategy();
    private final HttpClient httpClient;
    private final RequestBuilder queryRequestBuilder;
    private final RetryStrategy retryStrategy;

    /**
     * Construct a new FaunaClient instance with the provided FaunaConfig and HttpClient. This allows
     * the user to have complete control over HTTP Configuration, like timeouts, thread pool size,
     * and so-on.
     *
     * @param faunaConfig   The Fauna configuration settings.
     * @param httpClient    A Java HTTP client instance.
     * @param retryStrategy
     */
    public FaunaClient(FaunaConfig faunaConfig,
                       HttpClient httpClient, RetryStrategy retryStrategy) {
        this.httpClient = httpClient;
        if (Objects.isNull(faunaConfig)) {
            throw new IllegalArgumentException("FaunaConfig cannot be null.");
        } else if (Objects.isNull(httpClient)) {
            throw new IllegalArgumentException("HttpClient cannot be null.");
        } else {
            this.queryRequestBuilder = RequestBuilder.queryRequestBuilder(faunaConfig);
        }
        this.retryStrategy = retryStrategy;
    }

    /**
     * Construct a new FaunaClient instance with the provided FaunaConfig, using default HTTP config and retry
     * strategy.
     *
     * @param faunaConfig The Fauna configuration settings.
     */
    public FaunaClient(FaunaConfig faunaConfig) {
        this(faunaConfig, HttpClient.newBuilder().build(), DEFAULT_RETRY_STRATEGY);
    }


    private static <T> Supplier<CompletableFuture<QuerySuccess<T>>> makeAsyncRequest(HttpClient client, HttpRequest request, Codec<T> codec) {
        return () -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(body -> QueryResponse.handleResponse(body, codec));
    }

    /**
     * Sends an asynchronous Fauna Query Language (FQL) query to Fauna.
     *
     *     CompletableFuture<QuerySuccess<Document>> future = client.asyncQuery(fql, Document.class, null);
     *     ... do some other stuff ...
     *     Document doc = future.get().getData();
     *
     * @param fql               The FQL query to be executed.
     * @param resultClass       The expected class of the query result.
     * @param options           A (nullable) set of options to pass to the query.
     * @return QuerySuccess     The successful query result.
     * @throws FaunaException   If the query does not succeed, an exception will be thrown.
     */
    public <T> CompletableFuture<QuerySuccess<T>> asyncQuery(Query fql, Class<T> resultClass, QueryOptions options) {
        if (Objects.isNull(fql)) {
            throw new IllegalArgumentException("The provided FQL query is null.");
        }
        Codec<T> codec = CodecProvider.generate(new MappingContext(), resultClass);
        return new RetryHandler<QuerySuccess<T>>(this.retryStrategy).execute(makeAsyncRequest(
                this.httpClient, queryRequestBuilder.buildRequest(fql, options), codec));
    }

    /**
     * Sends an asynchronous Fauna Query Language (FQL) query to Fauna.
     *
     *     CompletableFuture<QuerySuccess<Document>> future = client.asyncQuery(fql, Document.class, null);
     *     ... do some other stuff ...
     *     Document doc = future.get().getData();
     *
     * @param fql               The FQL query to be executed.
     * @param codec             The codec to use.
     * @param options           A (nullable) set of options to pass to the query.
     * @return QuerySuccess     The successful query result.
     * @throws FaunaException   If the query does not succeed, an exception will be thrown.
     */
    public <T> CompletableFuture<QuerySuccess<T>> asyncQuery(Query fql, Codec<T> codec, QueryOptions options) {
        if (Objects.isNull(fql)) {
            throw new IllegalArgumentException("The provided FQL query is null.");
        }
        return new RetryHandler<QuerySuccess<T>>(this.retryStrategy).execute(makeAsyncRequest(
                this.httpClient, queryRequestBuilder.buildRequest(fql, options), codec));
    }

    /**
     * Sends an asynchronous Fauna Query Language (FQL) query to Fauna.
     *
     *     CompletableFuture<QuerySuccess<Document>> future = client.asyncQuery(fql, Document.class, null);
     *     ... do some other stuff ...
     *     Document doc = future.get().getData();
     *
     * @param fql               The FQL query to be executed.
     * @param resultClass       The expected class of the query result.
     * @return QuerySuccess     The successful query result.
     * @throws FaunaException   If the query does not succeed, an exception will be thrown.
     */
    public <T> CompletableFuture<QuerySuccess<T>> asyncQuery(Query fql, Class<T> resultClass) {
        return asyncQuery(fql, resultClass, null);
    }

    /**
     * Sends a Fauna Query Language (FQL) query to Fauna and returns the result.
     *
     *     QuerySuccess<Document> result = client.query(fql, Document.class, null);
     *     Document doc = result.getData();
     *
     * @param fql               The FQL query to be executed.
     * @param resultClass       The expected class of the query result.
     * @return QuerySuccess     The successful query result.
     * @throws FaunaException   If the query does not succeed, an exception will be thrown.
     */
    public <T> QuerySuccess<T> query(Query fql, Class<T> resultClass) throws FaunaException {
        try {
            return this.asyncQuery(fql, resultClass, null).get();
        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof FaunaException) {
                throw (FaunaException) e.getCause();
            } else {
                throw new ClientException("Unhandled exception.", e);
            }
        }
    }

    /**
     * Sends a Fauna Query Language (FQL) query to Fauna and returns the result.
     *
     *     QuerySuccess<Document> result = client.query(fql, Document.class, null);
     *     Document doc = result.getData();
     *
     * @param fql               The FQL query to be executed.
     * @param codec             The codec.
     * @return QuerySuccess     The successful query result.
     * @throws FaunaException   If the query does not succeed, an exception will be thrown.
     */
    public <T> QuerySuccess<T> query(Query fql, Codec<T> codec) throws FaunaException {
        try {
            return this.asyncQuery(fql, codec, null).get();
        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof FaunaException) {
                throw (FaunaException) e.getCause();
            } else {
                throw new ClientException("Unhandled exception.", e);
            }
        }
    }
}

