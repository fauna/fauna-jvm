package com.fauna.common.connection;

import com.fauna.common.configuration.FaunaConfig;
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
import static com.fauna.common.connection.Headers.TRACE_PARENT;
import static com.fauna.common.connection.Headers.TYPE_CHECK;

class RequestBuilder {

    private final FaunaConfig faunaConfig;
    private final Auth auth;
    private final DriverEnvironment driverEnvironment;
    private final URI uri;

    public static Builder builder() {
        return new Builder();
    }

    private RequestBuilder(Builder builder) {
        this.faunaConfig = builder.faunaConfig;
        uri = URI.create(faunaConfig.getEndpoint());
        this.auth = new Auth(faunaConfig.getSecret());
        this.driverEnvironment = new DriverEnvironment();
    }

    public HttpRequest buildRequest(String fql) {
        Map<String, String> headers = buildHeaders();
        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(faunaConfig.getQueryTimeout())
                .POST(HttpRequest.BodyPublishers.ofString(fql));
        headers.forEach(httpRequestBuilder::header);
        return httpRequestBuilder.build();
    }

    private Map<String, String> buildHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put(AUTHORIZATION, auth.bearer());
        headers.put(FORMAT, "tagged");
        headers.put(ACCEPT_ENCODING, "gzip");
        headers.put(CONTENT_TYPE, "application/json;charset=utf-8");
        headers.put(DRIVER, "Java");
        headers.put(DRIVER_ENV, driverEnvironment.toString());

        if (faunaConfig.getLinearized() != null) {
            headers.put(LINEARIZED, faunaConfig.getLinearized().toString());
        }

        if (faunaConfig.getTypeCheck() != null) {
            headers.put(TYPE_CHECK, faunaConfig.getTypeCheck().toString());
        }

        if (faunaConfig.getQueryTags() != null) {
            headers.put(QUERY_TAGS, QueryTags.encode(faunaConfig.getQueryTags()));
        }

        if (faunaConfig.getTraceParent() != null) {
            headers.put(TRACE_PARENT, faunaConfig.getTraceParent());
        }

        return headers;
    }

    public static class Builder {
        private FaunaConfig faunaConfig;

        public Builder faunaConfig(FaunaConfig faunaConfig) {
            this.faunaConfig = faunaConfig;
            return this;
        }

        RequestBuilder build() {
            return new RequestBuilder(this);
        }
    }
}
