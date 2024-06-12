package com.fauna.common.connection;

import com.fauna.common.configuration.FaunaConfig;
import com.fauna.common.configuration.JvmDriver;
import com.fauna.common.encoding.QueryTags;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;

import static com.fauna.common.connection.Headers.ACCEPT_ENCODING;
import static com.fauna.common.connection.Headers.AUTHORIZATION;
import static com.fauna.common.connection.Headers.CONTENT_TYPE;
import static com.fauna.common.connection.Headers.DRIVER;
import static com.fauna.common.connection.Headers.DRIVER_ENV;
import static com.fauna.common.connection.Headers.FORMAT;
import static com.fauna.common.connection.Headers.LINEARIZED;
import static com.fauna.common.connection.Headers.QUERY_TAGS;
import static com.fauna.common.connection.Headers.QUERY_TIMEOUT_MS;
import static com.fauna.common.connection.Headers.TRACE_PARENT;
import static com.fauna.common.connection.Headers.TYPE_CHECK;

/**
 * The RequestBuilder class is responsible for building HTTP requests for communicating with Fauna.
 */
class RequestBuilder {

    private final FaunaConfig faunaConfig;
    private final Auth auth;
    private final DriverEnvironment driverEnvironment;
    private final URI uri;

    /**
     * Creates a new builder for RequestBuilder.
     *
     * @return A new instance of Builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Private constructor for RequestBuilder.
     *
     * @param builder The builder used to create the RequestBuilder instance.
     */
    private RequestBuilder(Builder builder) {
        this.faunaConfig = builder.faunaConfig;
        uri = URI.create(faunaConfig.getEndpoint());
        this.auth = new Auth(faunaConfig.getSecret());
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

    /**
     * Builds and returns a map of HTTP headers required for the Fauna request.
     *
     * @return A Map of header names to header values.
     */
    private Map<String, String> buildHeaders() {
        // TODO: Do we need to rebuild this HashMap every time?
        Map<String, String> headers = new HashMap<>();
        headers.put(AUTHORIZATION, auth.bearer());
        // TODO: Put these constants somewhere.
        headers.put(FORMAT, "tagged");
        headers.put(ACCEPT_ENCODING, "gzip");
        headers.put(CONTENT_TYPE, "application/json;charset=utf-8");
        headers.put(DRIVER, "Java");
        headers.put(DRIVER_ENV, driverEnvironment.toString());
        headers.put(QUERY_TIMEOUT_MS, String.valueOf(faunaConfig.getQueryTimeout().toMillis()));

        if (faunaConfig.getLinearized().isPresent()) {
            headers.put(LINEARIZED, faunaConfig.getLinearized().get().toString());
        }
        if (faunaConfig.getTypeCheck().isPresent()) {
            headers.put(TYPE_CHECK, faunaConfig.getTypeCheck().get().toString());
        }

        if (!faunaConfig.getQueryTags().isEmpty()) {
            headers.put(QUERY_TAGS, QueryTags.encode(faunaConfig.getQueryTags()));
        }

        if (faunaConfig.getTraceParent().isPresent()) {
            headers.put(TRACE_PARENT, faunaConfig.getTraceParent().get());
        }

        return headers;
    }

    /**
     * Builder class for RequestBuilder. Follows the Builder Design Pattern.
     */
    public static class Builder {
        private FaunaConfig faunaConfig;
        private JvmDriver jvmDriver;

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
        public Builder jvmDriver(JvmDriver jvmDriver) {
            this.jvmDriver = jvmDriver;
            return this;
        }

        /**
         * Builds and returns a new RequestBuilder instance.
         *
         * @return A new instance of RequestBuilder.
         */
        RequestBuilder build() {
            return new RequestBuilder(this);
        }
    }
}
