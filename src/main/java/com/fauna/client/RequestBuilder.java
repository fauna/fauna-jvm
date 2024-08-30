package com.fauna.client;

import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;
import com.fauna.env.DriverEnvironment;
import com.fauna.exception.ClientException;
import com.fauna.query.QueryOptions;
import com.fauna.stream.StreamRequest;
import com.fauna.query.builder.Query;
import com.fauna.codec.UTF8FaunaGenerator;

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

    public RequestBuilder(URI uri, String token) {
        // DriverEnvironment is not needed outside the constructor for now.
        DriverEnvironment env = new DriverEnvironment(DriverEnvironment.JvmDriver.JAVA);
        this.baseRequestBuilder = HttpRequest.newBuilder().uri(uri).headers(
                RequestBuilder.Headers.FORMAT, "tagged",
                RequestBuilder.Headers.ACCEPT_ENCODING, "gzip",
                RequestBuilder.Headers.CONTENT_TYPE, "application/json;charset=utf-8",
                RequestBuilder.Headers.DRIVER, "Java",
                RequestBuilder.Headers.DRIVER_ENV, env.toString(),
                Headers.AUTHORIZATION, buildAuthHeader(token)
        );
    }

    public RequestBuilder(HttpRequest.Builder builder) {
        this.baseRequestBuilder = builder;
    }

    public static RequestBuilder queryRequestBuilder(FaunaConfig config) {
        return new RequestBuilder(URI.create(config.getEndpoint() + QUERY_PATH), config.getSecret());
    }

    public static RequestBuilder streamRequestBuilder(FaunaConfig config) {
        return new RequestBuilder(URI.create(config.getEndpoint() + STREAM_PATH), config.getSecret());
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
    public HttpRequest buildRequest(Query fql, QueryOptions options, CodecProvider provider) {
        // TODO: I think we can avoid doing this copy if no new headers need to be set.
        HttpRequest.Builder builder = baseRequestBuilder.copy();
        if (options != null) {
            addOptionalHeaders(builder, options);
        }
        // TODO: set last-txn-ts and max-contention-retries.
        try {
            UTF8FaunaGenerator gen = new UTF8FaunaGenerator();
            gen.writeStartObject();
            gen.writeFieldName("query");
            Codec<Query> codec = provider.get(Query.class);
            codec.encode(gen, fql);
            gen.writeEndObject();
            String body = gen.serialize();
            return builder.POST(HttpRequest.BodyPublishers.ofString(body)).build();
        } catch (IOException e) {
            throw new ClientException("Unable to build Fauna Query request.", e);
        }
    }

    public HttpRequest buildStreamRequest(StreamRequest req, CodecProvider provider) {
        HttpRequest.Builder builder = baseRequestBuilder.copy();
        try {
            UTF8FaunaGenerator gen = new UTF8FaunaGenerator();
            gen.writeStartObject();
            gen.writeFieldName(FieldNames.TOKEN);
            gen.writeStringValue(req.getToken());
            // Only one of cursor / start_ts can be present, prefer cursor.
            if (req.getCursor().isPresent()) {
                gen.writeString(FieldNames.CURSOR, req.getCursor().get());
            } else if (req.getStartTs().isPresent()) {
                // Cannot use ifPresent(...) because writeLong can throw an IOException.
                gen.writeLong(FieldNames.START_TS, req.getStartTs().get());
            }
            gen.writeEndObject();
            String body = gen.serialize();
            return builder.POST(HttpRequest.BodyPublishers.ofString(body)).build();
        } catch (IOException e) {
            throw new ClientException("Unable to build Fauna Stream request.", e);
        }

    }

    private static String buildAuthHeader(String token) {
        return String.join(" ", RequestBuilder.BEARER, token);
    }

    /**
     * Adds optional headers to the HttpRequest.Builder from the given QueryOptions.
     * @param builder A HttpRequest.Builder that will have headers added to it.
     * @param options The QueryOptions (must not be null).
     */
    private static void addOptionalHeaders(HttpRequest.Builder builder, QueryOptions options) {
        options.getTimeoutMillis().ifPresent(val -> builder.header(Headers.QUERY_TIMEOUT_MS, String.valueOf(val)));
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
