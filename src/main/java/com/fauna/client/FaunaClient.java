package com.fauna.client;

import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.DefaultCodecRegistry;
import com.fauna.codec.ParameterizedOf;
import com.fauna.event.EventSource;
import com.fauna.event.FaunaStream;
import com.fauna.event.FeedIterator;
import com.fauna.event.FeedOptions;
import com.fauna.event.FeedPage;
import com.fauna.event.StreamOptions;
import com.fauna.exception.ClientException;
import com.fauna.exception.FaunaException;
import com.fauna.exception.ServiceException;
import com.fauna.query.AfterToken;
import com.fauna.query.QueryOptions;
import com.fauna.query.builder.Query;
import com.fauna.response.QueryResponse;
import com.fauna.response.QuerySuccess;
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

/**
 * A client to interact with the Fauna service, providing asynchronous and synchronous query execution,
 * pagination, and streaming features.
 */
public abstract class FaunaClient {

    public static final RetryStrategy DEFAULT_RETRY_STRATEGY =
            ExponentialBackoffStrategy.builder().build();
    public static final RetryStrategy NO_RETRY_STRATEGY = new NoRetryStrategy();
    private final String faunaSecret;
    private final CodecProvider codecProvider =
            new DefaultCodecProvider(new DefaultCodecRegistry());
    private final AtomicLong lastTransactionTs = new AtomicLong(-1);
    private final Logger logger;
    private final StatsCollector statsCollector;

    abstract RetryStrategy getRetryStrategy();

    abstract HttpClient getHttpClient();

    abstract RequestBuilder getRequestBuilder();

    abstract RequestBuilder getStreamRequestBuilder();

    abstract RequestBuilder getFeedRequestBuilder();

    /**
     * Constructs a FaunaClient with the provided secret and logger.
     *
     * @param secret The Fauna secret used for authentication.
     * @param logger The logger instance.
     * @param statsCollector A collector for tracking statistics.
     */
    public FaunaClient(final String secret, final Logger logger,
                       final StatsCollector statsCollector) {
        this.faunaSecret = secret;
        this.logger = logger;
        this.statsCollector = statsCollector;
    }

    /**
     * Constructs a FaunaClient with the provided secret and log handler.
     *
     * @param secret The Fauna secret used for authentication.
     * @param logHandler The handler to manage log outputs.
     * @param statsCollector A collector for tracking statistics.
     */
    public FaunaClient(final String secret, final Handler logHandler,
                       final StatsCollector statsCollector) {
        this.faunaSecret = secret;
        this.logger = Logger.getLogger(this.getClass().getCanonicalName());
        this.logger.addHandler(logHandler);
        this.logger.setLevel(logHandler.getLevel());
        this.statsCollector = statsCollector;
    }

    /**
     * Retrieves the Fauna secret used for authentication.
     *
     * @return The Fauna secret.
     */
    protected String getFaunaSecret() {
        return this.faunaSecret;
    }

    /**
     * Retrieves the logger used for logging Fauna client activity.
     *
     * @return The logger instance.
     */
    public Logger getLogger() {
        return this.logger;
    }

    /**
     * Retrieves the stats collector instance.
     *
     * @return The stats collector instance.
     */
    public StatsCollector getStatsCollector() {
        return this.statsCollector;
    }

    /**
     * Retrieves the last known transaction timestamp.
     *
     * @return An Optional containing the last transaction timestamp, if available.
     */
    public Optional<Long> getLastTransactionTs() {
        long ts = lastTransactionTs.get();
        return ts > 0 ? Optional.of(ts) : Optional.empty();
    }

    private static Optional<ServiceException> extractServiceException(
            final Throwable throwable) {
        if (throwable instanceof ServiceException) {
            return Optional.of((ServiceException) throwable);
        } else if (throwable.getCause() instanceof ServiceException) {
            return Optional.of((ServiceException) throwable.getCause());
        } else {
            return Optional.empty();
        }
    }

    private void updateTs(final QueryResponse resp) {
        Long newTs = resp.getLastSeenTxn();
        if (newTs != null) {
            this.lastTransactionTs.updateAndGet(
                    oldTs -> newTs > oldTs ? newTs : oldTs);
        }
    }

    private <T> void completeRequest(final QuerySuccess<T> success,
                                     final Throwable throwable) {
        if (success != null) {
            updateTs(success);
        } else if (throwable != null) {
            extractServiceException(throwable).ifPresent(
                    exc -> updateTs(exc.getResponse()));
        }
    }

    private <E> void completeFeedRequest(final FeedPage<E> success,
                                         final Throwable throwable) {
        // Feeds do not update the clients latest transaction timestamp.
        if (throwable != null) {
            extractServiceException(throwable).ifPresent(
                    exc -> updateTs(exc.getResponse()));
        }
    }

    private void logResponse(final HttpResponse<InputStream> response) {
        logger.fine(MessageFormat.format(
                "Fauna HTTP Response {0} from {1}, headers: {2}",
                response.statusCode(), response.uri(),
                headersAsString(response.headers())));
    }

    private <T> Supplier<CompletableFuture<QuerySuccess<T>>> makeAsyncRequest(
            final HttpClient client, final HttpRequest request, final Codec<T> codec) {
        return () -> client.sendAsync(request,
                HttpResponse.BodyHandlers.ofInputStream()).thenApply(
                response -> {
                    logResponse(response);
                    return QueryResponse.parseResponse(response, codec,
                            statsCollector);
                }).whenComplete(this::completeRequest);
    }

    private <E> Supplier<CompletableFuture<FeedPage<E>>> makeAsyncFeedRequest(
            final HttpClient client, final HttpRequest request, final Codec<E> codec) {
        return () -> client.sendAsync(request,
                HttpResponse.BodyHandlers.ofInputStream()).thenApply(
                response -> {
                    logResponse(response);
                    return FeedPage.parseResponse(response, codec,
                            statsCollector);
                }).whenComplete(this::completeFeedRequest);
    }

    private <R> R completeAsync(final CompletableFuture<R> future, final String executionMessage) {
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException exc) {
            if (exc.getCause() != null && exc.getCause() instanceof FaunaException) {
                throw (FaunaException) exc.getCause();
            } else {
                logger.warning(
                        "Execution|InterruptedException: " + exc.getMessage());
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
    public CompletableFuture<QuerySuccess<Object>> asyncQuery(final Query fql) {
        if (Objects.isNull(fql)) {
            throw new IllegalArgumentException(
                    "The provided FQL query is null.");
        }
        Codec<Object> codec = codecProvider.get(Object.class, null);
        return new RetryHandler<QuerySuccess<Object>>(getRetryStrategy(),
                logger).execute(makeAsyncRequest(
                getHttpClient(),
                getRequestBuilder().buildRequest(fql, null, codecProvider,
                        lastTransactionTs.get()), codec));
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
     *
     * @param <T> The return type of the query.
     */
    public <T> CompletableFuture<QuerySuccess<T>> asyncQuery(final Query fql,
                                                             final Class<T> resultClass,
                                                             final QueryOptions options) {
        if (Objects.isNull(fql)) {
            throw new IllegalArgumentException(
                    "The provided FQL query is null.");
        }
        Codec<T> codec = codecProvider.get(resultClass, null);
        return new RetryHandler<QuerySuccess<T>>(getRetryStrategy(),
                logger).execute(makeAsyncRequest(
                getHttpClient(),
                getRequestBuilder().buildRequest(fql, options, codecProvider,
                        lastTransactionTs.get()), codec));
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
     *
     * @param <E> The inner type for the parameterized wrapper.
     */
    public <E> CompletableFuture<QuerySuccess<E>> asyncQuery(final Query fql,
                                                             final ParameterizedOf<E> parameterizedType,
                                                             final QueryOptions options) {
        if (Objects.isNull(fql)) {
            throw new IllegalArgumentException(
                    "The provided FQL query is null.");
        }
        @SuppressWarnings("unchecked")
        Codec<E> codec =
                codecProvider.get((Class<E>) parameterizedType.getRawType(),
                        parameterizedType.getActualTypeArguments());
        return new RetryHandler<QuerySuccess<E>>(getRetryStrategy(),
                logger).execute(makeAsyncRequest(
                getHttpClient(),
                getRequestBuilder().buildRequest(fql, options, codecProvider,
                        lastTransactionTs.get()), codec));
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
     * @return QuerySuccess     A CompletableFuture that completes with the successful query result.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     *
     * @param <T> The return type of the query.
     */
    public <T> CompletableFuture<QuerySuccess<T>> asyncQuery(final Query fql,
                                                             final Class<T> resultClass) {
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
     * @param <E> The inner type for the parameterized wrapper.
     */
    public <E> CompletableFuture<QuerySuccess<E>> asyncQuery(final Query fql,
                                                             final ParameterizedOf<E> parameterizedType) {
        if (Objects.isNull(fql)) {
            throw new IllegalArgumentException(
                    "The provided FQL query is null.");
        }
        @SuppressWarnings("unchecked")
        Codec<E> codec =
                codecProvider.get((Class<E>) parameterizedType.getRawType(),
                        parameterizedType.getActualTypeArguments());
        return new RetryHandler<QuerySuccess<E>>(getRetryStrategy(),
                logger).execute(makeAsyncRequest(
                getHttpClient(),
                getRequestBuilder().buildRequest(fql, null, codecProvider,
                        lastTransactionTs.get()), codec));
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
    public QuerySuccess<Object> query(final Query fql) throws FaunaException {
        return completeAsync(asyncQuery(fql, Object.class, null),
                "Unable to execute query.");
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
     * @param <T> The return type of the query.
     */
    public <T> QuerySuccess<T> query(final Query fql, final Class<T> resultClass)
            throws FaunaException {
        return completeAsync(asyncQuery(fql, resultClass, null),
                QUERY_EXECUTION);
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
     * @param <E> The inner type for the parameterized wrapper.
     */
    public <E> QuerySuccess<E> query(final Query fql,
                                     final ParameterizedOf<E> parameterizedType)
            throws FaunaException {
        return completeAsync(asyncQuery(fql, parameterizedType),
                QUERY_EXECUTION);
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
     * @param <T> The return type of the query.
     */
    public <T> QuerySuccess<T> query(final Query fql, final Class<T> resultClass,
                                     final QueryOptions options)
            throws FaunaException {
        return completeAsync(asyncQuery(fql, resultClass, options),
                QUERY_EXECUTION);
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
     * @param <E> The inner type for the parameterized wrapper.
     */
    public <E> QuerySuccess<E> query(final Query fql,
                                     final ParameterizedOf<E> parameterizedType,
                                     final QueryOptions options)
            throws FaunaException {
        return completeAsync(asyncQuery(fql, parameterizedType, options),
                QUERY_EXECUTION);
    }
    //endregion

    //region Query Page API

    /**
     * Sends a query to Fauna that retrieves the Page<E> for the given page token.
     *
     * @param after        The page token (result of a previous paginated request).
     * @param elementClass The expected class of the query result.
     * @param options      A (nullable) set of options to pass to the query.
     * @param <E>          The type of the elements of the page.
     * @return A CompletableFuture that returns a QuerySuccess with data of type Page<E>.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     */
    public <E> CompletableFuture<QuerySuccess<Page<E>>> asyncQueryPage(
            final AfterToken after, final Class<E> elementClass, final QueryOptions options) {
        return this.asyncQuery(PageIterator.buildPageQuery(after),
                pageOf(elementClass), options);
    }

    /**
     * Sends a query to Fauna that retrieves the Page<E> for the given page token.
     *
     * @param after        The page token (result of a previous paginated request).
     * @param elementClass The expected class of the query result.
     * @param options      A (nullable) set of options to pass to the query.
     * @param <E>          The type of the elements of the page.
     * @return A QuerySuccess with data of type Page<E>.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     */
    public <E> QuerySuccess<Page<E>> queryPage(
            final AfterToken after, final Class<E> elementClass, final QueryOptions options) {
        return completeAsync(asyncQueryPage(after, elementClass, options),
                QUERY_PAGE);
    }
    //endregion

    //region Paginated API

    /**
     * Send a Fauna Query Language (FQL) query to Fauna and return a paginated result.
     *
     * @param fql          The FQL query to be executed.
     * @param elementClass The expected class of the query result.
     * @param options      A (nullable) set of options to pass to the query.
     * @param <E>          The type of the elements of the page.
     * @return QuerySuccess     The successful query result.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     */
    public <E> PageIterator<E> paginate(final Query fql, final Class<E> elementClass,
                                        final QueryOptions options) {
        return new PageIterator<>(this, fql, elementClass, options);
    }

    /**
     * Send a Fauna Query Language (FQL) query to Fauna and return a paginated result.
     *
     * @param fql The FQL query to be executed.
     * @return The successful query result.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     */
    public PageIterator<Object> paginate(final Query fql) {
        return paginate(fql, Object.class, null);
    }

    /**
     * Send a Fauna Query Language (FQL) query to Fauna and return a paginated result.
     *
     * @param fql     The FQL query to be executed.
     * @param options A (nullable) set of options to pass to the query.
     * @return The successful query result.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     */
    public PageIterator<Object> paginate(final Query fql, final QueryOptions options) {
        return paginate(fql, Object.class, options);
    }

    /**
     * Send a Fauna Query Language (FQL) query to Fauna and return a paginated result.
     *
     * @param fql          The FQL query to be executed.
     * @param elementClass The expected class of the query result.
     * @return QuerySuccess     The successful query result.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     * @param <E> The type for each element in a page.
     */
    public <E> PageIterator<E> paginate(final Query fql, final Class<E> elementClass) {
        return paginate(fql, elementClass, null);
    }
    //endregion


    //region Streaming API

    /**
     * Send a request to the Fauna stream endpoint, and return a CompletableFuture that completes with the FaunaStream
     * publisher.
     *
     * @param eventSource   The Event Source (e.g. token from `.eventSource()`).
     * @param streamOptions The Stream Options (including start timestamp, retry strategy).
     * @param elementClass  The target type into which event data will be deserialized.
     * @return CompletableFuture    A CompletableFuture of FaunaStream<E>.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     * @param <E> The type for data in an event.
     */
    public <E> CompletableFuture<FaunaStream<E>> asyncStream(
            final EventSource eventSource,
            final StreamOptions streamOptions,
            final Class<E> elementClass) {
        HttpRequest streamReq =
                getStreamRequestBuilder().buildStreamRequest(eventSource,
                        streamOptions);
        return getHttpClient().sendAsync(streamReq,
                        HttpResponse.BodyHandlers.ofPublisher())
                .thenCompose(response -> {
                    CompletableFuture<FaunaStream<E>> publisher =
                            new CompletableFuture<>();
                    FaunaStream<E> fstream = new FaunaStream<>(elementClass,
                            this.statsCollector);
                    response.body().subscribe(fstream);
                    publisher.complete(fstream);
                    return publisher;
                });
    }

    /**
     * Send a request to the Fauna stream endpoint to start a stream, and return a FaunaStream publisher.
     *
     * @param eventSource   The request object including a stream token, and optionally a cursor, or timestamp.
     * @param streamOptions The stream options.
     * @param elementClass  The expected class &lt;E&gt; of the stream events.
     * @return FaunaStream      A publisher, implementing Flow.Publisher&lt;StreamEvent&lt;E&gt;&gt; from the Java Flow
     * API.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     * @param <E> The type for data in an event.
     */
    public <E> FaunaStream<E> stream(final EventSource eventSource,
                                     final StreamOptions streamOptions,
                                     final Class<E> elementClass) {
        return completeAsync(
                asyncStream(eventSource, streamOptions, elementClass),
                STREAM_SUBSCRIPTION);
    }

    /**
     * Start a Fauna stream based on an FQL query, and return a CompletableFuture of the resulting FaunaStream
     * publisher. This method sends two requests, one to the query endpoint to get the stream token, and then another
     * to the stream endpoint. This method is equivalent to calling the query, then the stream methods on FaunaClient.
     * <p>
     * This method does not take QueryOptions, or StreamOptions as parameters. If you need specify either
     * query, or stream options; you can use the asyncQuery/asyncStream methods.
     *
     * @param fql          The FQL query to be executed. It must return an event source, e.g. ends in `.eventSource()`.
     * @param elementClass The expected class &lt;E&gt; of the stream events.
     * @return FaunaStream      A publisher, implementing Flow.Publisher&lt;StreamEvent&lt;E&gt;&gt; from the Java Flow
     * API.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     * @param <E> The type for data in an event.
     */
    public <E> CompletableFuture<FaunaStream<E>> asyncStream(final Query fql,
                                                             final Class<E> elementClass) {
        return this.asyncQuery(fql, EventSource.class)
                .thenApply(queryResponse ->
                        this.stream(queryResponse.getData(), StreamOptions.builder().build(), elementClass));
    }

    /**
     * Start a Fauna stream based on an FQL query. This method sends two requests, one to the query endpoint to get
     * the stream token, and then another request to the stream endpoint which return the FaunaStream publisher.
     *
     * <p>
     * Query = fql("Product.all().eventSource()");
     * QuerySuccess&lt;EventSource&gt; querySuccess = client.query(fql, EventSource.class);
     * EventSource source = querySuccess.getData();
     * FaunaStream&lt;Product&gt; faunaStream = client.stream(source, StreamOptions.DEFAULT, Product.class)
     *
     * @param fql          The FQL query to be executed. It must return a stream, e.g. ends in `.toStream()`.
     * @param elementClass The expected class &lt;E&gt; of the stream events.
     * @return FaunaStream      A publisher, implementing Flow.Publisher&lt;StreamEvent&lt;E&gt;&gt; from the Java Flow
     * API.
     * @throws FaunaException If the query does not succeed, an exception will be thrown.
     * @param <E> The type for data in an event.
     */
    public <E> FaunaStream<E> stream(final Query fql, final Class<E> elementClass) {
        return completeAsync(asyncStream(fql, elementClass),
                STREAM_SUBSCRIPTION);
    }
    //endregion

    //region Event feeds

    /**
     * Send a request to the Fauna feed endpoint, and return a CompletableFuture that completes with the feed page.
     *
     * @param eventSource  An EventSource object (e.g. token from `.eventSource()`)
     * @param feedOptions  The FeedOptions object (default options will be used if null).
     * @param elementClass The expected class &lt;E&gt; of the feed events.
     * @param <E>          The type for data in an event.
     * @return CompletableFuture    A CompletableFuture that completes with a FeedPage&lt;E&gt;.
     */
    public <E> CompletableFuture<FeedPage<E>> poll(final EventSource eventSource,
                                                   final FeedOptions feedOptions,
                                                   final Class<E> elementClass) {
        return new RetryHandler<FeedPage<E>>(getRetryStrategy(),
                logger).execute(makeAsyncFeedRequest(
                getHttpClient(),
                getFeedRequestBuilder().buildFeedRequest(eventSource,
                        feedOptions != null ? feedOptions : FeedOptions.DEFAULT),
                codecProvider.get(elementClass)));
    }

    /**
     * Return a CompletableFuture that completes with a FeedIterator based on an FQL query. This method sends two
     * requests, one to the query endpoint to get the event source token, and then another request to the feed endpoint
     * to get the first page of results.
     *
     * @param fql          The FQL query to be executed. It must return a token, e.g. ends in `.changesOn()`.
     * @param feedOptions  The FeedOptions object (must not be null).
     * @param elementClass The expected class &lt;E&gt; of the feed events.
     * @param <E>          The type for data in an event.
     * @return FeedIterator A CompletableFuture that completes with a feed iterator that returns pages of Feed events.
     */
    public <E> CompletableFuture<FeedIterator<E>> asyncFeed(final Query fql,
                                                            final FeedOptions feedOptions,
                                                            final Class<E> elementClass) {
        return this.asyncQuery(fql, EventSource.class).thenApply(
                success -> this.feed(
                        success.getData(),
                        feedOptions, elementClass));
    }

    /**
     * Return a FeedIterator based on an FQL query. This method sends two requests, one to the query endpoint to get
     * the stream/feed token, and then another request to the feed endpoint to get the first page of results.
     *
     * @param fql          The FQL query to be executed. It must return a token, e.g. ends in `.changesOn()`.
     * @param feedOptions  The Feed Op
     * @param elementClass The expected class &lt;E&gt; of the feed events.
     * @param <E>          The type for data in an event.
     * @return FeedIterator An iterator that returns pages of Feed events.
     */
    public <E> FeedIterator<E> feed(final Query fql, final FeedOptions feedOptions, final Class<E> elementClass) {
        return completeAsync(asyncFeed(fql, feedOptions, elementClass),
                FEED_SUBSCRIPTION);
    }

    /**
     * Send a request to the Feed endpoint and return a FeedIterator.
     *
     * @param eventSource  The Fauna Event Source.
     * @param feedOptions  The feed options.
     * @param elementClass The expected class &lt;E&gt; of the feed events.
     * @param <E>          The type for data in an event.
     * @return FeedIterator An iterator that returns pages of Feed events.
     */
    public <E> FeedIterator<E> feed(final EventSource eventSource,
                                    final FeedOptions feedOptions,
                                    final Class<E> elementClass) {
        return new FeedIterator<>(this, eventSource, feedOptions, elementClass);
    }

    //endregion
}
