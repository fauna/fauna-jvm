package com.fauna.client;

import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.DefaultCodecRegistry;
import com.fauna.exception.ClientException;
import com.fauna.exception.FaunaException;
import com.fauna.query.QueryOptions;
import com.fauna.stream.StreamRequest;
import com.fauna.query.StreamTokenResponse;
import com.fauna.query.builder.Query;
import com.fauna.response.QueryResponse;
import com.fauna.response.QuerySuccess;
import com.fauna.codec.ParameterizedOf;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public abstract class FaunaClient {

    public static final RetryStrategy DEFAULT_RETRY_STRATEGY = ExponentialBackoffStrategy.builder().build();
    public static final RetryStrategy NO_RETRY_STRATEGY = new NoRetryStrategy();
    private final String faunaSecret;
    private final CodecProvider codecProvider = new DefaultCodecProvider(new DefaultCodecRegistry());

    abstract RetryStrategy getRetryStrategy();
    abstract HttpClient getHttpClient();
    abstract RequestBuilder getRequestBuilder();
    abstract RequestBuilder getStreamRequestBuilder();

    public FaunaClient(String secret) {
        this.faunaSecret = secret;
    }

    protected String getFaunaSecret() {
        return this.faunaSecret;
    }

    private static <T> Supplier<CompletableFuture<QuerySuccess<T>>> makeAsyncRequest(HttpClient client, HttpRequest request, Codec<T> codec) {
        return () -> client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()).thenApply(body -> QueryResponse.parseResponse(body, codec));
    }

    //region Asynchronous API
    /**
     * Sends an asynchronous Fauna Query Language (FQL) query to Fauna.
     * <p>
     * var future = client.asyncQuery(fql);
     * ... do some other stuff ...
     * var result = future.get().getData();
     *
     * @param fql The FQL query to be executed.
     * @return QuerySuccess     The successful query result.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     */
    public CompletableFuture<QuerySuccess<Object>> asyncQuery(Query fql) {
        if (Objects.isNull(fql)) {
            throw new IllegalArgumentException("The provided FQL query is null.");
        }
        Codec<Object> codec = codecProvider.get(Object.class, null);
        return new RetryHandler<QuerySuccess<Object>>(getRetryStrategy()).execute(FaunaClient.makeAsyncRequest(
                getHttpClient(), getRequestBuilder().buildRequest(fql, null, codecProvider), codec));
    }

    /**
     * Sends an asynchronous Fauna Query Language (FQL) query to Fauna.
     * <p>
     * CompletableFuture&lt;QuerySuccess&lt;Document&gt;&gt; future = client.asyncQuery(fql, Document.class, null);
     * ... do some other stuff ...
     * Document doc = future.get().getData();
     *
     * @param fql         The FQL query to be executed.
     * @param resultClass The expected class of the query result.
     * @param options     A (nullable) set of options to pass to the query.
     * @return QuerySuccess     The successful query result.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     */
    public <T> CompletableFuture<QuerySuccess<T>> asyncQuery(Query fql, Class<T> resultClass, QueryOptions options) {
        if (Objects.isNull(fql)) {
            throw new IllegalArgumentException("The provided FQL query is null.");
        }
        Codec<T> codec = codecProvider.get(resultClass, null);
        return new RetryHandler<QuerySuccess<T>>(getRetryStrategy()).execute(FaunaClient.makeAsyncRequest(
                getHttpClient(), getRequestBuilder().buildRequest(fql, options, codecProvider), codec));
    }

    /**
     * Sends an asynchronous Fauna Query Language (FQL) query to Fauna.
     * <p>
     * CompletableFuture&lt;QuerySuccess&lt;List&lt;int&gt;&gt;&gt; future = client.asyncQuery(fql, Parameterized.listOf(int.class), null);
     * ... do some other stuff ...
     * List&lt;int&gt;&gt; data = future.get().getData();
     *
     * @param fql               The FQL query to be executed.
     * @param parameterizedType The expected class of the query result.
     * @param options           A (nullable) set of options to pass to the query.
     * @return QuerySuccess     The successful query result.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     */
    public <E> CompletableFuture<QuerySuccess<E>> asyncQuery(Query fql, ParameterizedOf<E> parameterizedType, QueryOptions options) {
        if (Objects.isNull(fql)) {
            throw new IllegalArgumentException("The provided FQL query is null.");
        }
        @SuppressWarnings("unchecked")
        Codec<E> codec = codecProvider.get((Class<E>) parameterizedType.getRawType(), parameterizedType.getActualTypeArguments());
        return new RetryHandler<QuerySuccess<E>>(getRetryStrategy()).execute(FaunaClient.makeAsyncRequest(
                getHttpClient(), getRequestBuilder().buildRequest(fql, options, codecProvider), codec));
    }

    /**
     * Sends an asynchronous Fauna Query Language (FQL) query to Fauna.
     * <p>
     * CompletableFuture&lt;QuerySuccess&lt;Document&gt;&gt; future = client.asyncQuery(fql, Document.class);
     * ... do some other stuff ...
     * Document doc = future.get().getData();
     *
     * @param fql         The FQL query to be executed.
     * @param resultClass The expected class of the query result.
     * @return QuerySuccess     The successful query result.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     */
    public <T> CompletableFuture<QuerySuccess<T>> asyncQuery(Query fql, Class<T> resultClass) {
        return asyncQuery(fql, resultClass, null);
    }

    /**
     * Sends an asynchronous Fauna Query Language (FQL) query to Fauna.
     * <p>
     * CompletableFuture&lt;QuerySuccess&lt;List&lt;int&gt;&gt;&gt; future = client.asyncQuery(fql, Parameterized.listOf(int.class));
     * ... do some other stuff ...
     * List&lt;int&gt;&gt; data = future.get().getData();
     *
     * @param fql               The FQL query to be executed.
     * @param parameterizedType The expected class of the query result.
     * @return QuerySuccess     The successful query result.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     */
    public <E> CompletableFuture<QuerySuccess<E>> asyncQuery(Query fql, ParameterizedOf<E> parameterizedType) {
        if (Objects.isNull(fql)) {
            throw new IllegalArgumentException("The provided FQL query is null.");
        }
        Codec<E> codec = codecProvider.get((Class<E>) parameterizedType.getRawType(), parameterizedType.getActualTypeArguments());
        return new RetryHandler<QuerySuccess<E>>(getRetryStrategy()).execute(FaunaClient.makeAsyncRequest(
                getHttpClient(), getRequestBuilder().buildRequest(fql, null, codecProvider), codec));
    }
    //endregion

    //region Synchronous API
    /**
     * Sends a Fauna Query Language (FQL) query to Fauna and returns the result.
     * <p>
     * var result = client.query(fql);
     * var data = result.getData();
     *
     * @param fql The FQL query to be executed.
     * @return QuerySuccess     The successful query result.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     */
    public QuerySuccess<Object> query(Query fql) throws FaunaException {
        try {
            return this.asyncQuery(fql, Object.class, null).get();
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
     * <p>
     * QuerySuccess&lt;Document&gt; result = client.query(fql, Document.class);
     * Document doc = result.getData();
     *
     * @param fql         The FQL query to be executed.
     * @param resultClass The expected class of the query result.
     * @return QuerySuccess     The successful query result.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
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
     * <p>
     * QuerySuccess&lt;List&lt;int&gt;&gt;&gt; result = client.query(fql, Parameterized.listOf(int.class));
     * List&lt;int&gt;&gt; data = result.getData();
     *
     * @param fql               The FQL query to be executed.
     * @param parameterizedType The expected class of the query result.
     * @return QuerySuccess     The successful query result.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     */
    public <E> QuerySuccess<E> query(Query fql, ParameterizedOf<E> parameterizedType) throws FaunaException {
        try {
            return this.asyncQuery(fql, parameterizedType, null).get();
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
     * <p>
     * QuerySuccess<Document> result = client.query(fql, Document.class, null);
     * Document doc = result.getData();
     *
     * @param fql         The FQL query to be executed.
     * @param resultClass The expected class of the query result.
     * @param options     A (nullable) set of options to pass to the query.
     * @return QuerySuccess     The successful query result.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     */
    public <T> QuerySuccess<T> query(Query fql, Class<T> resultClass, QueryOptions options) throws FaunaException {
        try {
            return this.asyncQuery(fql, resultClass, options).get();
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
     * <p>
     * QuerySuccess&lt;List&lt;int&gt;&gt;&gt; result = client.query(fql, Parameterized.listOf(int.class), null);
     * List&lt;int&gt;&gt; data = result.getData();
     *
     * @param fql               The FQL query to be executed.
     * @param parameterizedType The expected class of the query result.
     * @param options           A (nullable) set of options to pass to the query.
     * @return QuerySuccess     The successful query result.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     */
    public <E> QuerySuccess<E> query(Query fql, ParameterizedOf<E> parameterizedType, QueryOptions options) throws FaunaException {
        try {
            return this.asyncQuery(fql, parameterizedType, options).get();
        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof FaunaException) {
                throw (FaunaException) e.getCause();
            } else {
                throw new ClientException("Unhandled exception.", e);
            }
        }
    }
    //endregion

    /**
     * Send a Fauna Query Language (FQL) query to Fauna and return a paginated result.
     * @param fql               The FQL query to be executed.
     * @param elementClass      The expected class of the query result.
     * @return QuerySuccess     The successful query result.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     */
    public <E> PageIterator<E> paginate(Query fql, Class<E> elementClass) {
        return new PageIterator<>(this, fql, elementClass, null);
    }

    /**
     * Send a Fauna Query Language (FQL) query to Fauna and return a paginated result.
     * @param fql               The FQL query to be executed.
     * @param elementClass      The expected class of the query result.
     * @param options           A (nullable) set of options to pass to the query.
     * @return QuerySuccess     The successful query result.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     */
    public <E> PageIterator<E> paginate(Query fql, Class<E> elementClass, QueryOptions options) {
        return new PageIterator<>(this, fql, elementClass, options);
    }

    /**
     * Send a request to the Fauna stream endpoint, and return a CompletableFuture that completes with the FaunaStream
     * publisher.
     *
     * @param streamRequest         The request object including a stream token, and optionally a cursor, or timestamp.
     * @param elementClass          The expected class &lt;E&gt; of the stream events.
     * @return CompletableFuture    A CompletableFuture of FaunaStream<E>.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     */
    public <E> CompletableFuture<FaunaStream<E>> asyncStream(StreamRequest streamRequest, Class<E> elementClass) {
        HttpRequest streamReq = getStreamRequestBuilder().buildStreamRequest(streamRequest);
        return getHttpClient().sendAsync(streamReq,
                HttpResponse.BodyHandlers.ofPublisher()).thenCompose(response -> {
                    CompletableFuture<FaunaStream<E>> publisher = new CompletableFuture<>();
                    FaunaStream<E> fstream = new FaunaStream<>(elementClass);
                    response.body().subscribe(fstream);
                    publisher.complete(fstream);
                    return publisher;
                });
    }

    /**
     * Send a request to the Fauna stream endpoint to start a stream, and return a FaunaStream publisher.
     * @param streamRequest     The request object including a stream token, and optionally a cursor, or timestamp.
     * @param elementClass      The expected class &lt;E&gt; of the stream events.
     * @return FaunaStream      A publisher, implementing Flow.Publisher&lt;StreamEvent&lt;E&gt;&gt; from the Java Flow API.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     */
    public <E> FaunaStream<E> stream(StreamRequest streamRequest, Class<E> elementClass) {
        try {
            return this.asyncStream(streamRequest, elementClass).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ClientException("Unable to subscribe to stream.", e);
        }

    }

    /**
     * Start a Fauna stream based on an FQL query, and return a CompletableFuture of the resulting FaunaStream
     * publisher. This method sends two requests, one to the query endpoint to get the stream token, and then another
     * to the stream endpoint. This method is equivalent to calling the query, then the stream methods on FaunaClient.
     *
     * @param fql               The FQL query to be executed. It must return a stream, e.g. ends in `.toStream()`.
     * @param elementClass      The expected class &lt;E&gt; of the stream events.
     * @return FaunaStream      A publisher, implementing Flow.Publisher&lt;StreamEvent&lt;E&gt;&gt; from the Java Flow API.
     * @throws FaunaException   If the query does not succeed, an exception will be thrown.
     */
    public <E> CompletableFuture<FaunaStream<E>> asyncStream(Query fql, Class<E> elementClass) {
        return this.asyncQuery(fql, StreamTokenResponse.class).thenApply(
                queryResponse -> this.stream(StreamRequest.fromTokenResponse(queryResponse.getData()), elementClass));
    }

    /**
     *
     * Start a Fauna stream based on an FQL query. This method sends two requests, one to the query endpoint to get
     * the stream token, and then another request to the stream endpoint which return the FaunaStream publisher.
     *
     * <p>
     * Query = fql("Product.all().toStream()");
     * QuerySuccess&lt;StreamTokenResponse&gt; tokenResp = client.query(fql, StreamTokenResponse.class);
     * FaunaStream&lt;Product&gt; faunaStream = client.stream(new StreamRequest(tokenResp.getData.getToken(), Product.class)
     *
     * @param fql               The FQL query to be executed. It must return a stream, e.g. ends in `.toStream()`.
     * @param elementClass      The expected class &lt;E&gt; of the stream events.
     * @return FaunaStream      A publisher, implementing Flow.Publisher&lt;StreamEvent&lt;E&gt;&gt; from the Java Flow API.
     * @throws FaunaException   If the query does not succeed, an exception will be thrown.
     */
    public <E> FaunaStream<E> stream(Query fql, Class<E> elementClass) {
        try {
            return this.asyncStream(fql, elementClass).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ClientException("Unable to subscribe to stream.", e);
        }

    }
}
