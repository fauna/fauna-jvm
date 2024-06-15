package com.fauna.common.connection;

import com.fauna.common.configuration.FaunaConfig;
import com.fauna.common.encoding.QueryTags;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.*;

/**
 * The RequestBuilder class is responsible for building HTTP requests for communicating with Fauna.
 */
public class RequestBuilder {

    private static final String BEARER = "Bearer";

    private final FaunaConfig faunaConfig;
    private final DriverEnvironment driverEnvironment;
    private final URI uri;
    private final String[][] headers;
    private final HttpRequest.Builder httpRequestBuilder;


    class Headers {
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
        this.uri = URI.create(faunaConfig.getEndpoint());
        this.headers = this.buildHeaders();
        this.httpRequestBuilder = HttpRequest.newBuilder().uri(this.uri);
        for (String[] hdr : this.headers) {
            httpRequestBuilder.header(hdr[0], hdr[1]);
        }
    }

    /**
     * Builds and returns an HTTP request for a given Fauna query string (FQL).
     *
     * @param fql The Fauna query string.
     * @return An HttpRequest object configured for the Fauna query.
     */
    public HttpRequest buildRequest(String fql) {
        return this.httpRequestBuilder.POST(HttpRequest.BodyPublishers.ofString(fql)).build();
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

        if (faunaConfig.getLinearized().isPresent()) {
            headerList.add(new String[] {RequestBuilder.Headers.LINEARIZED, faunaConfig.getLinearized().get().toString()});
        }
        if (faunaConfig.getTypeCheck().isPresent()) {
            headerList.add(new String[] {RequestBuilder.Headers.TYPE_CHECK, faunaConfig.getTypeCheck().get().toString()});
        }

        if (!faunaConfig.getQueryTags().isEmpty()) {
            headerList.add(new String[] {RequestBuilder.Headers.QUERY_TAGS, QueryTags.encode(faunaConfig.getQueryTags())});
        }

        if (faunaConfig.getTraceParent().isPresent()) {
            headerList.add(new String[] {RequestBuilder.Headers.TRACE_PARENT, faunaConfig.getTraceParent().get()});
        }
        return headerList.toArray(new String[headerList.size()][2]);
    }
}
