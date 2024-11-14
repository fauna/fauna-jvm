package com.fauna.client;

import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.env.DriverEnvironment;
import com.fauna.event.EventSource;
import com.fauna.event.FeedOptions;
import com.fauna.event.FeedRequest;
import com.fauna.event.StreamOptions;
import com.fauna.event.StreamRequest;
import com.fauna.exception.ClientException;
import com.fauna.query.QueryOptions;
import com.fauna.query.builder.Query;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.logging.Logger;

import static com.fauna.client.Logging.headersAsString;

/**
 * The RequestBuilder class is responsible for building HTTP requests for communicating with Fauna.
 */
public final class RequestBuilder {

    private static final String BEARER = "Bearer";
    private static final String QUERY_PATH = "/query/1";
    private static final String STREAM_PATH = "/stream/1";
    private static final String FEED_PATH = "/feed/1";

    private final HttpRequest.Builder baseRequestBuilder;
    private final Duration clientTimeoutBuffer;
    private final Logger logger;

    /**
     * Field names for HTTP requests.
     */
    public static class FieldNames {
        static final String QUERY = "query";
        public static final String TOKEN = "token";
        public static final String CURSOR = "cursor";
        public static final String START_TS = "start_ts";
        public static final String PAGE_SIZE = "page_size";
    }

    /**
     * HTTP headers used for Fauna requests.
     */
    static class Headers {
        static final String LAST_TXN_TS = "X-Last-Txn-Ts";
        static final String LINEARIZED = "X-Linearized";
        static final String MAX_CONTENTION_RETRIES = "X-Max-Contention-Retries";
        static final String QUERY_TIMEOUT_MS = "X-Query-Timeout-Ms";
        static final String TYPE_CHECK = "X-Typecheck";
        static final String QUERY_TAGS = "X-Query-Tags";
        static final String TRACE_PARENT = "Traceparent";
        static final String ACCEPT_ENCODING = "Accept-Encoding";
        static final String AUTHORIZATION = "Authorization";
        static final String CONTENT_TYPE = "Content-Type";
        static final String DRIVER = "X-Driver";
        static final String DRIVER_ENV = "X-Driver-Env";
        static final String FORMAT = "X-Format";
    }

    /**
     * Constructor for creating a RequestBuilder with the specified Fauna configuration.
     *
     * @param uri                   The URI for the Fauna endpoint.
     * @param token                 The secret key used for authorization.
     * @param maxContentionRetries  The maximum retries for contention errors.
     * @param clientTimeoutBuffer   The buffer for the client timeout.
     * @param logger                The logger to log HTTP request details.
     */
    public RequestBuilder(final URI uri, final String token, final int maxContentionRetries,
                          final Duration clientTimeoutBuffer, final Logger logger) {
        DriverEnvironment env =
                new DriverEnvironment(DriverEnvironment.JvmDriver.JAVA);
        this.baseRequestBuilder = HttpRequest.newBuilder().uri(uri).headers(
                RequestBuilder.Headers.FORMAT, "tagged",
                RequestBuilder.Headers.ACCEPT_ENCODING, "gzip",
                RequestBuilder.Headers.CONTENT_TYPE,
                "application/json;charset=utf-8",
                RequestBuilder.Headers.DRIVER, "Java",
                RequestBuilder.Headers.DRIVER_ENV, env.toString(),
                RequestBuilder.Headers.MAX_CONTENTION_RETRIES,
                String.valueOf(maxContentionRetries),
                Headers.AUTHORIZATION, buildAuthHeader(token)
        );
        this.clientTimeoutBuffer = clientTimeoutBuffer;
        this.logger = logger;
    }

    /**
     * Constructor for creating a RequestBuilder with an existing HttpRequest.Builder.
     *
     * @param builder              The HttpRequest.Builder to use.
     * @param clientTimeoutBuffer  The buffer for the client timeout.
     * @param logger               The logger to log HTTP request details.
     */
    public RequestBuilder(final HttpRequest.Builder builder,
                          final Duration clientTimeoutBuffer, final Logger logger) {
        this.baseRequestBuilder = builder;
        this.clientTimeoutBuffer = clientTimeoutBuffer;
        this.logger = logger;
    }

    /**
     * Creates a new RequestBuilder for Fauna queries.
     *
     * @param config The FaunaConfig containing endpoint and secret.
     * @param logger The logger for logging HTTP request details.
     * @return A new instance of RequestBuilder.
     */
    public static RequestBuilder queryRequestBuilder(final FaunaConfig config,
                                                     final Logger logger) {
        return new RequestBuilder(URI.create(config.getEndpoint() + QUERY_PATH),
                config.getSecret(), config.getMaxContentionRetries(),
                config.getClientTimeoutBuffer(), logger);
    }

    /**
     * Creates a new RequestBuilder for Fauna streams.
     *
     * @param config The FaunaConfig containing endpoint and secret.
     * @param logger The logger for logging HTTP request details.
     * @return A new instance of RequestBuilder.
     */
    public static RequestBuilder streamRequestBuilder(final FaunaConfig config,
                                                      final Logger logger) {
        return new RequestBuilder(
                URI.create(config.getEndpoint() + STREAM_PATH),
                config.getSecret(), config.getMaxContentionRetries(),
                config.getClientTimeoutBuffer(), logger);
    }

    /**
     * Creates a new RequestBuilder for Fauna feed requests.
     *
     * @param config The FaunaConfig containing endpoint and secret.
     * @param logger The logger for logging HTTP request details.
     * @return A new instance of RequestBuilder.
     */
    public static RequestBuilder feedRequestBuilder(final FaunaConfig config,
                                                    final Logger logger) {
        return new RequestBuilder(URI.create(config.getEndpoint() + FEED_PATH),
                config.getSecret(), config.getMaxContentionRetries(),
                config.getClientTimeoutBuffer(), logger);
    }

    /**
     * Creates a scoped request builder with the given token.
     *
     * @param token The token to be used for the request's authorization header.
     * @return A new instance of RequestBuilder with the scoped token.
     */
    public RequestBuilder scopedRequestBuilder(final String token) {
        HttpRequest.Builder newBuilder = this.baseRequestBuilder.copy();
        newBuilder.setHeader(Headers.AUTHORIZATION, buildAuthHeader(token));
        return new RequestBuilder(newBuilder, clientTimeoutBuffer, logger);
    }

    private void logRequest(final String body, final HttpRequest req) {
        String timeout = req.timeout().map(
                val -> MessageFormat.format(" (timeout: {0})", val)).orElse("");
        logger.fine(MessageFormat.format(
                "Fauna HTTP {0} Request to {1}{2}, headers: {3}",
                req.method(), req.uri(), timeout,
                headersAsString(req.headers())));
        logger.finest("Request body: " + body);
    }

    /**
     * Builds and returns an HTTP request for a given Fauna query string (FQL).
     *
     * @param fql        The Fauna query string.
     * @param options    The query options.
     * @param provider   The codec provider to encode the query.
     * @param lastTxnTs  The last transaction timestamp (optional).
     * @return An HttpRequest object configured for the Fauna query.
     */
    public HttpRequest buildRequest(final Query fql, final QueryOptions options,
                                    final CodecProvider provider, final Long lastTxnTs) {
        HttpRequest.Builder builder = getBuilder(options, lastTxnTs);
        try (UTF8FaunaGenerator gen = UTF8FaunaGenerator.create()) {
            gen.writeStartObject();
            gen.writeFieldName(FieldNames.QUERY);
            Codec<Query> codec = provider.get(Query.class);
            codec.encode(gen, fql);
            gen.writeEndObject();
            String body = gen.serialize();
            HttpRequest req =
                    builder.POST(HttpRequest.BodyPublishers.ofString(body))
                            .build();
            logRequest(body, req);
            return req;
        }
    }

    /**
     * Builds and returns an HTTP request for a Fauna stream.
     *
     * @param eventSource The event source for the stream.
     * @param streamOptions The stream options.
     * @return An HttpRequest object configured for the Fauna stream.
     */
    public HttpRequest buildStreamRequest(final EventSource eventSource,
                                          final StreamOptions streamOptions) {
        HttpRequest.Builder builder = baseRequestBuilder.copy();
        streamOptions.getTimeout().ifPresent(builder::timeout);
        try {
            String body = new StreamRequest(eventSource, streamOptions).serialize();
            HttpRequest req = builder.POST(HttpRequest.BodyPublishers.ofString(body)).build();
            logRequest(body, req);
            return req;
        } catch (IOException e) {
            throw new ClientException("Unable to build Fauna Stream request.", e);
        }
    }

    /**
     * Builds and returns an HTTP request for a Fauna feed.
     *
     * @param eventSource The event source for the feed.
     * @param options     The feed options.
     * @return An HttpRequest object configured for the Fauna feed.
     */
    public HttpRequest buildFeedRequest(final EventSource eventSource,
                                        final FeedOptions options) {
        FeedRequest request = new FeedRequest(eventSource, options);
        HttpRequest.Builder builder = baseRequestBuilder.copy();
        options.getTimeout().ifPresent(val -> {
            builder.timeout(val.plus(clientTimeoutBuffer));
            builder.header(Headers.QUERY_TIMEOUT_MS,
                    String.valueOf(val.toMillis()));
        });
        try {
            String body = request.serialize();
            HttpRequest req = builder.POST(HttpRequest.BodyPublishers.ofString(request.serialize())).build();
            logRequest(body, req);
            return req;
        } catch (IOException e) {
            throw new ClientException("Unable to build Fauna Feed request.", e);
        }
    }

    /**
     * Builds an authorization header for the given token.
     *
     * @param token The token to be used in the authorization header.
     * @return The authorization header value.
     */
    private static String buildAuthHeader(final String token) {
        return String.join(" ", RequestBuilder.BEARER, token);
    }

    /**
     * Gets the base request builder or a copy with options applied.
     *
     * @param options   The QueryOptions (must not be null).
     * @param lastTxnTs The last transaction timestamp (optional).
     * @return The HttpRequest.Builder configured with options.
     */
    private HttpRequest.Builder getBuilder(final QueryOptions options, final Long lastTxnTs) {
        if (options == null && (lastTxnTs == null || lastTxnTs <= 0)) {
            return baseRequestBuilder;
        }
        HttpRequest.Builder builder = baseRequestBuilder.copy();
        if (lastTxnTs != null) {
            builder.setHeader(Headers.LAST_TXN_TS, String.valueOf(lastTxnTs));
        }
        if (options != null) {

            options.getTimeoutMillis().ifPresent(val -> {
                builder.timeout(Duration.ofMillis(val).plus(clientTimeoutBuffer));
                builder.header(Headers.QUERY_TIMEOUT_MS, String.valueOf(val));
            });
            options.getLinearized().ifPresent(
                    val -> builder.header(Headers.LINEARIZED, String.valueOf(val)));
            options.getTypeCheck().ifPresent(
                    val -> builder.header(Headers.TYPE_CHECK, String.valueOf(val)));
            options.getTraceParent().ifPresent(
                    val -> builder.header(Headers.TRACE_PARENT, val));
            options.getQueryTags().ifPresent(
                    val -> builder.headers(Headers.QUERY_TAGS, val.encode()));
        }
        return builder;
    }

}
