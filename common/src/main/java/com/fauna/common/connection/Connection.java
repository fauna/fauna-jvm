package com.fauna.common.connection;

import com.fauna.common.configuration.FaunaConfig;
import com.fauna.common.configuration.HttpClientConfig;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class Connection {

    private final RequestBuilder requestBuilder;
    private final HttpClientConfig httpClientConfig;
    private final HttpClient httpClient;

    public static Builder builder() {
        return new Builder();
    }

    private Connection(Builder builder) {
        this.httpClientConfig = builder.httpClientConfig;
        this.requestBuilder = RequestBuilder.builder()
                .faunaConfig(builder.faunaConfig)
                .build();
        this.httpClient = createHttpClient();
    }

    Connection(RequestBuilder requestBuilder, HttpClientConfig httpClientConfig, HttpClient httpClient) {
        this.requestBuilder = requestBuilder;
        this.httpClientConfig = httpClientConfig;
        this.httpClient = httpClient;
    }

    private HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(httpClientConfig.getConnectTimeout())
                .executor(Executors.newFixedThreadPool(httpClientConfig.getMaxConnections()))
                .build();
    }

    public CompletableFuture<HttpResponse<String>> performRequest(String fql) {
        HttpRequest request = requestBuilder.buildRequest(fql);
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public static class Builder {
        private FaunaConfig faunaConfig;
        private HttpClientConfig httpClientConfig;

        public Builder faunaConfig(FaunaConfig faunaConfig) {
            this.faunaConfig = faunaConfig;
            return this;
        }

        public Builder httpClientConfig(HttpClientConfig httpClientConfig) {
            this.httpClientConfig = httpClientConfig;
            return this;
        }

        public Connection build() {
            return new Connection(this);
        }
    }

}
