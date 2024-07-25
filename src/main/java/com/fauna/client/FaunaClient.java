package com.fauna.client;

import com.fauna.exception.ClientException;
import com.fauna.exception.FaunaException;
import com.fauna.interfaces.IDeserializer;
import com.fauna.mapping.MappingContext;
import com.fauna.query.QueryOptions;
import com.fauna.query.builder.Query;
import com.fauna.response.QueryResponse;
import com.fauna.response.QuerySuccess;
import com.fauna.serialization.Deserializer;
import com.fauna.types.Document;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
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
            this.queryRequestBuilder = RequestBuilder.queryRequestBuilder(faunaConfig);
        }
        this.retryStrategy = DEFAULT_RETRY_STRATEGY;
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

    public static <T> Supplier<CompletableFuture<QuerySuccess<T>>> makeAsyncRequest(HttpClient client, HttpRequest request, IDeserializer<T> deserializer) {
        return () -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(body -> QueryResponse.handleResponse(body, deserializer));
    }

    /**
     * Sends a Fauna Query Language (FQL) query to Fauna.
     *
     * @param fql The FQL query to be executed.
     * @return QuerySuccess
     * @throws FaunaException If the provided FQL query is null.
     */
    public <T> CompletableFuture<QuerySuccess<T>> asyncQuery(Query fql, QueryOptions options, RetryStrategy strategy, Class<T> resultClass) {
        if (Objects.isNull(fql)) {
            throw new IllegalArgumentException("The provided FQL query is null.");
        }
        IDeserializer<T> deserializer = Deserializer.generate(new MappingContext(), resultClass);
        return new RetryHandler<QuerySuccess<T>>(strategy).execute(makeAsyncRequest(
                this.httpClient, queryRequestBuilder.buildRequest(fql, options), deserializer));
    }

    public CompletableFuture<QuerySuccess<Document>> asyncQuery(Query fql) {
        return asyncQuery(fql, null, this.retryStrategy, Document.class);
    }

    /**
     * Sends a Fauna Query Language (FQL) query to Fauna.
     *
     * @param fql The FQL query to be executed.
     * @return QuerySuccess
     * @throws FaunaException If the provided FQL query is null.
     */
    public <T> CompletableFuture<QuerySuccess<T>> asyncQuery(Query fql, QueryOptions options, Class<T> resultClass) {
        return asyncQuery(fql, options, this.retryStrategy, resultClass);
    }

    public <T> CompletableFuture<QuerySuccess<T>> asyncQuery(Query fql, Class<T> resultClass) {
        return asyncQuery(fql, null, this.retryStrategy, resultClass);
    }

    public <T> QuerySuccess<T> query(Query fql, Class<T> resultClass) throws FaunaException {
        try {
            return this.asyncQuery(fql, null, resultClass).get();
        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof FaunaException) {
                throw (FaunaException) e.getCause();
            } else {
                throw new ClientException("Unhandled exception.", e);
            }
        }
    }

    public QuerySuccess<Document> query(Query fql) throws FaunaException {
        return this.query(fql, Document.class);
    }


}

