package com.fauna.client;

import com.fauna.common.connection.DriverEnvironment;
import com.fauna.exception.ClientException;
import com.fauna.query.QueryOptions;
import com.fauna.query.builder.Query;
import com.fauna.serialization.Serializer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The RequestBuilder class is responsible for building HTTP requests for communicating with Fauna.
 */
public class RequestBuilder {

    private static final String BEARER = "Bearer";

    private final FaunaConfig faunaConfig;
    private final DriverEnvironment driverEnvironment;
    private final HttpRequest.Builder baseRequestBuilder;


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

    public RequestBuilder(FaunaConfig config) {
        this.faunaConfig = config;
        this.driverEnvironment = new DriverEnvironment(DriverEnvironment.JvmDriver.JAVA);
        this.baseRequestBuilder = HttpRequest.newBuilder().uri(URI.create(faunaConfig.getEndpoint())).headers(
                RequestBuilder.Headers.FORMAT, "tagged",
                RequestBuilder.Headers.ACCEPT_ENCODING, "gzip",
                RequestBuilder.Headers.CONTENT_TYPE, "application/json;charset=utf-8",
                RequestBuilder.Headers.DRIVER, "Java",
                RequestBuilder.Headers.DRIVER_ENV, driverEnvironment.toString(),
                Headers.AUTHORIZATION, buildAuthToken()
        );
    }

    /**
     * Builds and returns an HTTP request for a given Fauna query string (FQL).
     *
     * @param fql The Fauna query string.
     * @return An HttpRequest object configured for the Fauna query.
     */
    public HttpRequest buildRequest(Query fql, QueryOptions options) {
        HttpRequest.Builder builder = baseRequestBuilder.copy();
        if (options != null) {
            addOptionalHeaders(builder, options);
        }
        // TODO: set last-txn-ts and max-contention-retries.
        try {
            return builder.POST(HttpRequest.BodyPublishers.ofString(
                    Serializer.serialize(new FaunaRequest(fql)))).build();
        } catch (IOException e) {
            throw new ClientException("Unable to build Fauna Query request.", e);
        }
    }

    private String buildAuthToken() {
        return String.join(" ", RequestBuilder.BEARER, this.faunaConfig.getSecret());
    }

    /**
     * Adds optional headers to the HttpRequest.Builder from the given QueryOptions.
     * @param builder A HttpRequest.Builder that will have headers added to it.
     * @param options The QueryOptions (must not be null).
     */
    private static void addOptionalHeaders(HttpRequest.Builder builder, QueryOptions options) {
        options.getTimeout().ifPresent(val -> builder.header(Headers.QUERY_TIMEOUT_MS, String.valueOf(val)));
        options.getLinearized().ifPresent(val -> builder.header(Headers.LINEARIZED, String.valueOf(val)));
        options.getTypeCheck().ifPresent(val -> builder.header(Headers.TYPE_CHECK, String.valueOf(val)));
        options.getTraceParent().ifPresent(val -> builder.header(Headers.TRACE_PARENT, val));
        options.getQueryTags().ifPresent(val -> builder.headers(Headers.QUERY_TAGS, QueryTags.encode(val)));
    }

    public static class QueryTags {
        private static final String EQUALS = "=";
        private static final String COMMA = ",";

        public static String encode(Map<String, String> tags) {
            return tags.entrySet().stream()
                    .map(entry -> String.join(EQUALS, entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining(COMMA));
        }

    }
}