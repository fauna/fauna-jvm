package com.fauna.client;

import com.fauna.common.connection.DriverEnvironment;
import com.fauna.query.builder.Query;
import com.fauna.serialization.Serializer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The RequestBuilder class is responsible for building HTTP requests for communicating with Fauna.
 */
public class RequestBuilder {

    private static final String BEARER = "Bearer";

    private final FaunaConfig faunaConfig;
    private final DriverEnvironment driverEnvironment;
    private final HttpRequest.Builder httpRequestBuilder;


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
        this.httpRequestBuilder = HttpRequest.newBuilder().uri(URI.create(faunaConfig.getEndpoint()));
        for (String[] hdr : this.buildHeaders()) {
            httpRequestBuilder.header(hdr[0], hdr[1]);
        }
    }

    /**
     * Builds and returns an HTTP request for a given Fauna query string (FQL).
     *
     * @param fql The Fauna query string.
     * @return An HttpRequest object configured for the Fauna query.
     */
    public HttpRequest buildRequest(Query fql) {

        try {
            return this.httpRequestBuilder.POST(HttpRequest.BodyPublishers.ofString(Serializer.serialize(new FaunaRequest(fql)))).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildAuthToken() {
        return String.join(" ", new String[]{RequestBuilder.BEARER, this.faunaConfig.getSecret()});
    }

    /**
     * Builds and returns a map of HTTP headers required for the Fauna request.
     *
     * @return A Map of header names to header values.
     */
    private String[][] buildHeaders() {
        ArrayList<String[]> headerList = new ArrayList<>(Arrays.asList(
                new String[] {RequestBuilder.Headers.AUTHORIZATION, this.buildAuthToken()},
                new String[] {RequestBuilder.Headers.FORMAT, "tagged"},
                new String[] {RequestBuilder.Headers.ACCEPT_ENCODING, "gzip"},
                new String[] {RequestBuilder.Headers.CONTENT_TYPE, "application/json;charset=utf-8"},
                new String[] {RequestBuilder.Headers.DRIVER, "Java"},
                new String[] {RequestBuilder.Headers.DRIVER_ENV, driverEnvironment.toString()},
                new String[] {RequestBuilder.Headers.QUERY_TIMEOUT_MS,
                        String.valueOf(faunaConfig.getQueryTimeout().toMillis())}
        ));

        faunaConfig.getLinearized().ifPresent(l -> headerList.add(new String[] {Headers.LINEARIZED, l.toString()}));
        faunaConfig.getTypeCheck().ifPresent(tc -> headerList.add(new String[] {Headers.TYPE_CHECK, tc.toString()}));
        faunaConfig.getTraceParent().ifPresent(tp -> headerList.add(new String[] {Headers.TRACE_PARENT, tp}));

        if (!faunaConfig.getQueryTags().isEmpty()) {
            headerList.add(new String[] {RequestBuilder.Headers.QUERY_TAGS,
                    QueryTags.encode(faunaConfig.getQueryTags())});
        }
        return headerList.toArray(new String[headerList.size()][2]);
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
