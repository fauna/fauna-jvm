package com.fauna.common.connection;

import com.fauna.common.configuration.FaunaConfig;
import com.fauna.common.encoding.QueryTags;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * The RequestBuilder class is responsible for building HTTP requests for communicating with Fauna.
 */
public class RequestBuilder {


    private final FaunaConfig faunaConfig;
    private final DriverEnvironment driverEnvironment;
    private final URI uri;
    private static final String BEARER = "Bearer";


    /**
     * Creates a new builder for RequestBuilder.
     *
     * @return A new instance of Builder.
     */
    public static Builder builder() {
        return new Builder();
    }

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
    /**
     * Private constructor for RequestBuilder.
     *
     * @param builder The builder used to create the RequestBuilder instance.
     */
    private RequestBuilder(Builder builder) {
        this.faunaConfig = builder.faunaConfig;
        uri = URI.create(faunaConfig.getEndpoint());
        this.driverEnvironment = new DriverEnvironment(builder.jvmDriver);
    }

    /**
     * Builds and returns an HTTP request for a given Fauna query string (FQL).
     *
     * @param fql The Fauna query string.
     * @return An HttpRequest object configured for the Fauna query.
     */
    public HttpRequest buildRequest(String fql) {
        Map<String, String> headers = buildHeaders();
        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(fql));
        headers.forEach(httpRequestBuilder::header);
        return httpRequestBuilder.build();
    }

    private String buildAuthToken() {
        return String.join(" ", new String[]{RequestBuilder.BEARER, this.faunaConfig.getSecret()});
    }

    /**
     * Builds and returns a map of HTTP headers required for the Fauna request.
     *
     * @return A Map of header names to header values.
     */
    private Map<String, String> buildHeaders() {
        // TODO: Do we need to rebuild this HashMap every time?
        Map<String, String> headers = new HashMap<>();
        headers.put(RequestBuilder.Headers.AUTHORIZATION, this.buildAuthToken());
        // TODO: Put these constants somewhere.
        headers.put(RequestBuilder.Headers.FORMAT, "tagged");
        headers.put(RequestBuilder.Headers.ACCEPT_ENCODING, "gzip");
        headers.put(RequestBuilder.Headers.CONTENT_TYPE, "application/json;charset=utf-8");
        headers.put(RequestBuilder.Headers.DRIVER, "Java");
        headers.put(RequestBuilder.Headers.DRIVER_ENV, driverEnvironment.toString());
        headers.put(RequestBuilder.Headers.QUERY_TIMEOUT_MS, String.valueOf(faunaConfig.getQueryTimeout().toMillis()));

        if (faunaConfig.getLinearized().isPresent()) {
            headers.put(RequestBuilder.Headers.LINEARIZED, faunaConfig.getLinearized().get().toString());
        }
        if (faunaConfig.getTypeCheck().isPresent()) {
            headers.put(RequestBuilder.Headers.TYPE_CHECK, faunaConfig.getTypeCheck().get().toString());
        }

        if (!faunaConfig.getQueryTags().isEmpty()) {
            headers.put(RequestBuilder.Headers.QUERY_TAGS, QueryTags.encode(faunaConfig.getQueryTags()));
        }

        if (faunaConfig.getTraceParent().isPresent()) {
            headers.put(RequestBuilder.Headers.TRACE_PARENT, faunaConfig.getTraceParent().get());
        }

        return headers;
    }

    /**
     * Builder class for RequestBuilder. Follows the Builder Design Pattern.
     */
    public static class Builder {
        private FaunaConfig faunaConfig;
        private DriverEnvironment.JvmDriver jvmDriver;

        /**
         * Sets the FaunaConfig for the RequestBuilder.
         *
         * @param faunaConfig The configuration settings for Fauna.
         * @return The current Builder instance.
         */
        public Builder faunaConfig(FaunaConfig faunaConfig) {
            this.faunaConfig = faunaConfig;
            return this;
        }

        /**
         * Sets the JvmDriver for the RequestBuilder.
         *
         * @param jvmDriver The JVM driver information.
         * @return The current Builder instance.
         */
        public Builder jvmDriver(DriverEnvironment.JvmDriver jvmDriver) {
            this.jvmDriver = jvmDriver;
            return this;
        }

        /**
         * Builds and returns a new RequestBuilder instance.
         *
         * @return A new instance of RequestBuilder.
         */
        public RequestBuilder build() {
            return new RequestBuilder(this);
        }
    }
}
