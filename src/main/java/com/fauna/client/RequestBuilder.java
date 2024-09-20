package com.fauna.client;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;
import com.fauna.env.DriverEnvironment;
import com.fauna.exception.ClientException;
import com.fauna.query.QueryOptions;
import com.fauna.stream.StreamRequest;
import com.fauna.query.builder.Query;
import com.fauna.codec.UTF8FaunaGenerator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;

/**
 * The RequestBuilder class is responsible for building HTTP requests for communicating with Fauna.
 */
public class RequestBuilder {

    private static final String BEARER = "Bearer";
    private static final String QUERY_PATH = "/query/1";
    private static final String STREAM_PATH = "/stream/1";

    private final HttpRequest.Builder baseRequestBuilder;

    static class FieldNames {
        static final String QUERY = "query";
        static final String TOKEN = "token";
        static final String CURSOR = "cursor";
        static final String START_TS = "start_ts";
    }

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

    public RequestBuilder(URI uri, String token, int max_contention_retries) {
        // DriverEnvironment is not needed outside the constructor for now.
        DriverEnvironment env = new DriverEnvironment(DriverEnvironment.JvmDriver.JAVA);
        this.baseRequestBuilder = HttpRequest.newBuilder().uri(uri).headers(
                RequestBuilder.Headers.FORMAT, "tagged",
                RequestBuilder.Headers.ACCEPT_ENCODING, "gzip",
                RequestBuilder.Headers.CONTENT_TYPE, "application/json;charset=utf-8",
                RequestBuilder.Headers.DRIVER, "Java",
                RequestBuilder.Headers.DRIVER_ENV, env.toString(),
                RequestBuilder.Headers.MAX_CONTENTION_RETRIES, String.valueOf(max_contention_retries),
                Headers.AUTHORIZATION, buildAuthHeader(token)
        );
    }

    public RequestBuilder(HttpRequest.Builder builder) {
        this.baseRequestBuilder = builder;
    }

    public static RequestBuilder queryRequestBuilder(FaunaConfig config) {
        return new RequestBuilder(URI.create(config.getEndpoint() + QUERY_PATH), config.getSecret(), config.getMaxContentionRetries());
    }

    public static RequestBuilder streamRequestBuilder(FaunaConfig config) {
        return new RequestBuilder(URI.create(config.getEndpoint() + STREAM_PATH), config.getSecret(), config.getMaxContentionRetries());
    }

    public RequestBuilder scopedRequestBuilder(String token) {
        HttpRequest.Builder newBuilder = this.baseRequestBuilder.copy();
        // .setHeader(..) clears existing headers (which we want) while .header(..) would append it :)
        newBuilder.setHeader(Headers.AUTHORIZATION, buildAuthHeader(token));
        return new RequestBuilder(newBuilder);
    }

    /**
     * Builds and returns an HTTP request for a given Fauna query string (FQL).
     *
     * @param fql The Fauna query string.
     * @return An HttpRequest object configured for the Fauna query.
     */
    public HttpRequest buildRequest(Query fql, QueryOptions options, CodecProvider provider, Long last_txn_ts) {
        HttpRequest.Builder builder = getBuilder(options, last_txn_ts);
        try (UTF8FaunaGenerator gen = UTF8FaunaGenerator.create()) {
            gen.writeStartObject();
            gen.writeFieldName(FieldNames.QUERY);
            Codec<Query> codec = provider.get(Query.class);
            codec.encode(gen, fql);
            gen.writeEndObject();
            String body = gen.serialize();
            return builder.POST(HttpRequest.BodyPublishers.ofString(body)).build();
        }
    }

    public String buildStreamRequestBody(StreamRequest request) throws IOException {
        // Use JsonGenerator directly rather than UTF8FaunaGenerator because this is not FQL. For example,
        // start_ts is a JSON numeric/integer, not a tagged '@long'.
        ByteArrayOutputStream requestBytes = new ByteArrayOutputStream();
        JsonGenerator gen = new JsonFactory().createGenerator(requestBytes);
        gen.writeStartObject();
        gen.writeStringField(FieldNames.TOKEN, request.getToken());
        // Only one of cursor / start_ts can be present, prefer cursor.
        // Cannot use ifPresent(val -> ...) because gen.write methods can throw an IOException.
        if (request.getCursor().isPresent()) {
            gen.writeStringField(FieldNames.CURSOR, request.getCursor().get());
        } else if (request.getStartTs().isPresent()) {
            gen.writeNumberField(FieldNames.START_TS, request.getStartTs().get());
        }
        gen.writeEndObject();
        gen.flush();
        return requestBytes.toString(StandardCharsets.UTF_8);
    }

    public HttpRequest buildStreamRequest(StreamRequest request) {
        HttpRequest.Builder builder = baseRequestBuilder.copy();
        try {
            return builder.POST(HttpRequest.BodyPublishers.ofString(buildStreamRequestBody(request))).build();
        } catch (IOException e) {
            throw new ClientException("Unable to build Fauna Stream request.", e);
        }

    }

    private static String buildAuthHeader(String token) {
        return String.join(" ", RequestBuilder.BEARER, token);
    }

    /**
     * Get either the base request builder (if options is null) or a copy with the options applied.
     * @param options The QueryOptions (must not be null).
     */
    private HttpRequest.Builder getBuilder(QueryOptions options, Long lastTxnTs) {
        if (options == null && (last_txn_ts == null || last_txn_ts <= 0) ) {
            return baseRequestBuilder;
        }
        HttpRequest.Builder builder = baseRequestBuilder.copy();
        if (last_txn_ts != null) {
            builder.setHeader(Headers.LAST_TXN_TS, String.valueOf(last_txn_ts));
        }
        if (options != null) {
            options.getTimeoutMillis().ifPresent(val -> builder.header(Headers.QUERY_TIMEOUT_MS, String.valueOf(val)));
            options.getLinearized().ifPresent(val -> builder.header(Headers.LINEARIZED, String.valueOf(val)));
            options.getTypeCheck().ifPresent(val -> builder.header(Headers.TYPE_CHECK, String.valueOf(val)));
            options.getTraceParent().ifPresent(val -> builder.header(Headers.TRACE_PARENT, val));
            options.getQueryTags().ifPresent(val -> builder.headers(Headers.QUERY_TAGS, val.encode()));
        }
        return builder;
    }

}
