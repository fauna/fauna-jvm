package com.fauna.client;

import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.DefaultCodecRegistry;
import com.fauna.event.FaunaStream;
import com.fauna.event.FeedIterator;
import com.fauna.event.StreamOptions;
import com.fauna.exception.ClientException;
import com.fauna.exception.ErrorHandler;
import com.fauna.exception.FaunaException;
import com.fauna.exception.ProtocolException;
import com.fauna.exception.ServiceException;
import com.fauna.event.EventSource;
import com.fauna.event.FeedOptions;
import com.fauna.event.FeedPage;
import com.fauna.query.AfterToken;
import com.fauna.query.QueryOptions;
import com.fauna.response.QueryFailure;
import com.fauna.event.StreamRequest;
import com.fauna.event.EventSourceResponse;
import com.fauna.query.builder.Query;
import com.fauna.response.QueryResponse;
import com.fauna.response.QuerySuccess;
import com.fauna.codec.ParameterizedOf;
import com.fauna.types.Page;

import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Logger;

import static com.fauna.client.Logging.headersAsString;
import static com.fauna.codec.Generic.pageOf;
import static com.fauna.constants.ErrorMessages.FEED_SUBSCRIPTION;
import static com.fauna.constants.ErrorMessages.QUERY_EXECUTION;
import static com.fauna.constants.ErrorMessages.QUERY_PAGE;
import static com.fauna.constants.ErrorMessages.STREAM_SUBSCRIPTION;

public abstract class FaunaClient {

    public static final RetryStrategy DEFAULT_RETRY_STRATEGY = ExponentialBackoffStrategy.builder().build();
    public static final RetryStrategy NO_RETRY_STRATEGY = new NoRetryStrategy();
    private final String faunaSecret;
    private final CodecProvider codecProvider = new DefaultCodecProvider(new DefaultCodecRegistry());
    private final AtomicLong lastTransactionTs = new AtomicLong(-1);
    private final Logger logger;
    private final StatsCollector statsCollector;

    abstract RetryStrategy getRetryStrategy();
    abstract HttpClient getHttpClient();
    abstract RequestBuilder getRequestBuilder();
    abstract RequestBuilder getStreamRequestBuilder();
    abstract RequestBuilder getFeedRequestBuilder();

    public FaunaClient(String secret, Logger logger, StatsCollector statsCollector) {
        this.faunaSecret = secret;
        this.logger = logger;
        this.statsCollector = statsCollector;
    }

    public FaunaClient(String secret, Handler logHandler, StatsCollector statsCollector) {
        this.faunaSecret = secret;
        this.logger = Logger.getLogger(this.getClass().getCanonicalName());
        this.logger.addHandler(logHandler);
        this.logger.setLevel(logHandler.getLevel());
        this.statsCollector = statsCollector;
    }

    protected String getFaunaSecret() {
        return this.faunaSecret;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public StatsCollector getStatsCollector() {
        return this.statsCollector;
    }

    public Optional<Long> getLastTransactionTs() {
        long ts = lastTransactionTs.get();
        return ts > 0 ? Optional.of(ts) : Optional.empty();
    }

    private static Optional<ServiceException> extractServiceException(Throwable throwable) {
        if (throwable instanceof ServiceException) {
            return Optional.of((ServiceException) throwable);
        } else if (throwable.getCause() instanceof ServiceException) {
            return Optional.of((ServiceException) throwable.getCause());
        } else {
            return Optional.empty();
        }
    }

    private void updateTs(QueryResponse resp) {
        Long newTs = resp.getLastSeenTxn();
        if (newTs != null) {
            this.lastTransactionTs.updateAndGet(oldTs -> newTs > oldTs ? newTs : oldTs );
        }
    }

    private <T> void completeRequest(QuerySuccess<T> success, Throwable throwable) {
        if (success != null) {
            updateTs(success);
        } else if (throwable != null) {
            extractServiceException(throwable).ifPresent(exc -> updateTs(exc.getResponse()));
        }
    }

    private <E> void completeFeedRequest(FeedPage<E> success, Throwable throwable) {
        // Feeds do not update the clients latest transaction timestamp.
        if (throwable != null) {
            extractServiceException(throwable).ifPresent(exc -> updateTs(exc.getResponse()));
        }
    }

    private void logResponse(HttpResponse<InputStream> response) {
        logger.fine(MessageFormat.format("Fauna HTTP Response {0} from {1}, headers: {2}",
                response.statusCode(), response.uri(), headersAsString(response.headers())));
        // We could implement a LoggingInputStream or something to log the response here.
    }

    private <T> Supplier<CompletableFuture<QuerySuccess<T>>> makeAsyncRequest(HttpClient client, HttpRequest request, Codec<T> codec) {
        return () -> client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()).thenApply(
                response -> {
                    logResponse(response);
                    return QueryResponse.parseResponse(response, codec, statsCollector);
                }).whenComplete(this::completeRequest);
    }

    private <E> Supplier<CompletableFuture<FeedPage<E>>> makeAsyncFeedRequest(HttpClient client, HttpRequest request, Codec<E> codec) {
        return () -> client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()).thenApply(
                response -> {
                    logResponse(response);
                    if (response.statusCode() >= 400) {
                        // There are possibly some different error cases to handle for feeds. This seems like
                        // a comprehensive solution for now. In the future we could rename QueryFailure et. al. to
                        // something like FaunaFailure, or implement "FeedFailure".
                        QueryFailure failure = new QueryFailure(response.statusCode(), QueryResponse.builder(codec));
                        ErrorHandler.handleQueryFailure(response.statusCode(), failure);
                        // Fall back on ProtocolException.
                        throw new ProtocolException(response.statusCode(), failure);
                    }
                    return FeedPage.parseResponse(response, codec);
                }).whenComplete(this::completeFeedRequest);
    }

    private <R> R completeAsync(CompletableFuture<R> future, String executionMessage) {
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException exc) {
            if (exc.getCause() != null && exc.getCause() instanceof FaunaException) {
                throw (FaunaException) exc.getCause();
            } else {
                logger.warning("Execution|InterruptedException: " + exc.getMessage());
                throw new ClientException(executionMessage, exc);
            }
        }
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
        return new RetryHandler<QuerySuccess<Object>>(getRetryStrategy(), logger).execute(makeAsyncRequest(
                getHttpClient(), getRequestBuilder().buildRequest(fql, null, codecProvider, lastTransactionTs.get()), codec));
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
        return new RetryHandler<QuerySuccess<T>>(getRetryStrategy(), logger).execute(makeAsyncRequest(
                getHttpClient(), getRequestBuilder().buildRequest(fql, options, codecProvider, lastTransactionTs.get()), codec));
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
        return new RetryHandler<QuerySuccess<E>>(getRetryStrategy(), logger).execute(makeAsyncRequest(
                getHttpClient(), getRequestBuilder().buildRequest(fql, options, codecProvider, lastTransactionTs.get()), codec));
    }

    /**
     * Sends an asynchronous Fauna Query Language (FQL) query to Fauna.
     * <p>
     * CompletableFuture&lt;QuerySuccess&lt;Document&gt;&gt; future = client.asyncQuery(fql, Document.class);
     * ... do some other stuff ...
     * Document doc = future.get().getData();
     *
     * @param fql               The FQL query to be executed.
     * @param resultClass       The expected class of the query result.
     * @return QuerySuccess     A CompletableFuture that completes with the successful query result.
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
        @SuppressWarnings("unchecked")
        Codec<E> codec = codecProvider.get((Class<E>) parameterizedType.getRawType(), parameterizedType.getActualTypeArguments());
        return new RetryHandler<QuerySuccess<E>>(getRetryStrategy(), logger).execute(makeAsyncRequest(
                getHttpClient(), getRequestBuilder().buildRequest(fql, null, codecProvider, lastTransactionTs.get()), codec));
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
        return completeAsync(asyncQuery(fql, Object.class, null), "Unable to execute query.");
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
        return completeAsync(asyncQuery(fql, resultClass, null), QUERY_EXECUTION);
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
        return completeAsync(asyncQuery(fql, parameterizedType), QUERY_EXECUTION);
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
        return completeAsync(asyncQuery(fql, resultClass, options), QUERY_EXECUTION);
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
        return completeAsync(asyncQuery(fql, parameterizedType, options), QUERY_EXECUTION);
    }
    //endregion

    //region Query Page API

    /**
     * Sends a query to Fauna that retrieves the Page<E> for the given page token.
     * @param after         The page token (result of a previous paginated request).
     * @param elementClass  The expected class of the query result.
     * @param options       A (nullable) set of options to pass to the query.
     * @return              A CompletableFuture that returns a QuerySuccess with data of type Page<E>.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     * @param <E>           The type of the elements of the page.
     */
    public <E> CompletableFuture<QuerySuccess<Page<E>>> asyncQueryPage(
            AfterToken after, Class<E> elementClass, QueryOptions options) {
        return this.asyncQuery(PageIterator.buildPageQuery(after), pageOf(elementClass), options);
    }

    /**
     * Sends a query to Fauna that retrieves the Page<E> for the given page token.
     * @param after         The page token (result of a previous paginated request).
     * @param elementClass  The expected class of the query result.
     * @param options       A (nullable) set of options to pass to the query.
     * @return              A QuerySuccess with data of type Page<E>.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     * @param <E>           The type of the elements of the page.
     */
    public <E> QuerySuccess<Page<E>> queryPage(
            AfterToken after, Class<E> elementClass, QueryOptions options) {
        return completeAsync(asyncQueryPage(after, elementClass, options), QUERY_PAGE);
    }
    //endregion

    //region Paginated API
    /**
     * Send a Fauna Query Language (FQL) query to Fauna and return a paginated result.
     * @param fql               The FQL query to be executed.
     * @param elementClass      The expected class of the query result.
     * @param options           A (nullable) set of options to pass to the query.
     * @return QuerySuccess     The successful query result.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     * @param <E>           The type of the elements of the page.
     */
    public <E> PageIterator<E> paginate(Query fql, Class<E> elementClass, QueryOptions options) {
        return new PageIterator<>(this, fql, elementClass, options);
    }

    /**
     * Send a Fauna Query Language (FQL) query to Fauna and return a paginated result.
     * @param fql               The FQL query to be executed.
     * @return                  The successful query result.
     * @throws FaunaException   If the query does not succeed, an exception will be thrown.
     */
    public PageIterator<Object> paginate(Query fql) {
        return paginate(fql, Object.class, null);
    }

    /**
     * Send a Fauna Query Language (FQL) query to Fauna and return a paginated result.
     * @param fql               The FQL query to be executed.
     * @param options           A (nullable) set of options to pass to the query.
     * @return                  The successful query result.
     * @throws FaunaException   If the query does not succeed, an exception will be thrown.
     */
    public PageIterator<Object> paginate(Query fql, QueryOptions options) {
        return paginate(fql, Object.class, options);
    }

    /**
     * Send a Fauna Query Language (FQL) query to Fauna and return a paginated result.
     * @param fql               The FQL query to be executed.
     * @param elementClass      The expected class of the query result.
     * @return QuerySuccess     The successful query result.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     */
    public <E> PageIterator<E> paginate(Query fql, Class<E> elementClass) {
        return paginate(fql, elementClass, null);
    }
    //endregion


    //region Streaming API
    /**
     * Send a request to the Fauna stream endpoint, and return a CompletableFuture that completes with the FaunaStream
     * publisher.
     *
     * @param eventSource
     * @param streamOptions
     * @return CompletableFuture    A CompletableFuture of FaunaStream<E>.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     */
    public <E> CompletableFuture<FaunaStream<E>> asyncStream(EventSource eventSource, StreamOptions streamOptions, Class<E> elementClass) {
        HttpRequest streamReq = getStreamRequestBuilder().buildStreamRequest(eventSource, streamOptions);
        return getHttpClient().sendAsync(streamReq,
                HttpResponse.BodyHandlers.ofPublisher()).thenCompose(response -> {
                    CompletableFuture<FaunaStream<E>> publisher = new CompletableFuture<>();
                    FaunaStream<E> fstream = new FaunaStream<>(elementClass, this.statsCollector);
                    response.body().subscribe(fstream);
                    publisher.complete(fstream);
                    return publisher;
                });
    }

    /**
     * Send a request to the Fauna stream endpoint to start a stream, and return a FaunaStream publisher.
     *
     * @param eventSource   The request object including a stream token, and optionally a cursor, or timestamp.
     * @param streamOptions
     * @param elementClass  The expected class &lt;E&gt; of the stream events.
     * @return FaunaStream      A publisher, implementing Flow.Publisher&lt;StreamEvent&lt;E&gt;&gt; from the Java Flow API.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     */
    public <E> FaunaStream<E> stream(EventSource eventSource, StreamOptions streamOptions, Class<E> elementClass) {
        return completeAsync(asyncStream(eventSource, streamOptions, elementClass), STREAM_SUBSCRIPTION);
    }

    /**
     * Start a Fauna stream based on an FQL query, and return a CompletableFuture of the resulting FaunaStream
     * publisher. This method sends two requests, one to the query endpoint to get the stream token, and then another
     * to the stream endpoint. This method is equivalent to calling the query, then the stream methods on FaunaClient.
     *
     * This method does not take QueryOptions, or StreamOptions as parameters. If you need specify either
     * query, or stream options; you can use the asyncQuery/asyncStream methods.
     *
     * @param fql               The FQL query to be executed. It must return a stream, e.g. ends in `.toStream()`.
     * @param elementClass      The expected class &lt;E&gt; of the stream events.
     * @return FaunaStream      A publisher, implementing Flow.Publisher&lt;StreamEvent&lt;E&gt;&gt; from the Java Flow API.
     * @throws FaunaException   If the query does not succeed, an exception will be thrown.
     */
    public <E> CompletableFuture<FaunaStream<E>> asyncStream(Query fql, Class<E> elementClass) {
        return this.asyncQuery(fql, EventSourceResponse.class).thenApply(
                queryResponse -> this.stream(EventSource.fromResponse(queryResponse.getData()), StreamOptions.builder().build(), elementClass));
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
        return completeAsync(asyncStream(fql, elementClass), STREAM_SUBSCRIPTION);
    }
    //endregion

    //region Event Feeds

    /**
     * Send a request to the Fauna feed endpoint, and return a CompletableFuture that completes with the feed page.
     * @param source        An EventSource object with the feed token.
     * @param feedOptions   The FeedOptions object (default options will be used if null).
     * @param elementClass  The expected class &lt;E&gt; of the feed events.
     * @return FeedSuccess  A CompletableFuture that completes with the successful feed response.
     * @param <E>           The type of the feed events.
     */
    public <E> CompletableFuture<FeedPage<E>> poll(EventSource source, FeedOptions feedOptions, Class<E> elementClass) {
        return new RetryHandler<FeedPage<E>>(getRetryStrategy(), logger).execute(makeAsyncFeedRequest(
                getHttpClient(), getFeedRequestBuilder().buildFeedRequest(source, feedOptions != null ? feedOptions : FeedOptions.DEFAULT), codecProvider.get(elementClass)));
    }

    /**
     * Return a CompletableFuture that completes with a FeedIterator based on an FQL query. This method sends two
     * requests, one to the query endpoint to get the event source token, and then another request to the feed endpoint
     * to get the first page of results.
     * @param fql           The FQL query to be executed. It must return a token, e.g. ends in `.changesOn()`.
     * @param feedOptions   The FeedOptions object (must not be null).
     * @param elementClass  The expected class &lt;E&gt; of the feed events.
     * @return FeedIterator A CompletableFuture that completes with a feed iterator that returns pages of Feed events.
     * @param <E>           The type of the feed events.
     */
    public <E> CompletableFuture<FeedIterator<E>> asyncFeed(Query fql, FeedOptions feedOptions, Class<E> elementClass) {
        return this.asyncQuery(fql, EventSourceResponse.class).thenApply(success -> this.feed(EventSource.fromResponse(success.getData()), feedOptions, elementClass));
    }

    /**
     * Return a FeedIterator based on an FQL query. This method sends two requests, one to the query endpoint to get
     * the stream/feed token, and then another request to the feed endpoint to get the first page of results.
     * @param fql           The FQL query to be executed. It must return a token, e.g. ends in `.changesOn()`.
     * @param feedOptions   The Feed Op
     * @param elementClass  The expected class &lt;E&gt; of the feed events.
     * @return FeedIterator An iterator that returns pages of Feed events.
     * @param <E>           The type of the feed events.
     */
    public <E> FeedIterator<E> feed(Query fql, FeedOptions feedOptions, Class<E> elementClass) {
        return completeAsync(asyncFeed(fql, feedOptions, elementClass), FEED_SUBSCRIPTION);
    }

    /**
     * Send a request to the Feed endpoint and return a FeedIterator.
     * @param eventSource   The Fauna Event Source.
     * @param elementClass  The expected class &lt;E&gt; of the feed events.
     * @return FeedIterator An iterator that returns pages of Feed events.
     * @param <E>           The type of the feed events.
     */
    public <E> FeedIterator<E> feed(EventSource eventSource, FeedOptions feedOptions, Class<E> elementClass) {
        return new FeedIterator<>(this, eventSource, feedOptions, elementClass);
    }

    //endregion
}
