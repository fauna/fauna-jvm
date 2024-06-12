package com.fauna.common.connection;

import com.fauna.common.configuration.FaunaConfig;
import com.fauna.common.configuration.HttpClientConfig;
import com.fauna.common.configuration.JvmDriver;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/**
 * The Connection class is responsible for establishing and managing the connection to Fauna.
 * It utilizes the Java HTTP client to send requests and receive responses.
 * TODO: is this required?
 */
public class Connection {

    private final RequestBuilder requestBuilder;
    private final HttpClientConfig httpClientConfig;
    private final HttpClient httpClient;

    /**
     * Creates a new builder for Connection.
     *
     * @return A new instance of Builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Private constructor for Connection.
     *
     * @param builder The builder used to create the Connection instance.
     */
    private Connection(Builder builder) {
        this.httpClientConfig = builder.httpClientConfig;
        this.requestBuilder = RequestBuilder.builder()
                .faunaConfig(builder.faunaConfig)
                .jvmDriver(builder.jvmDriver)
                .build();
        this.httpClient = createHttpClient();
    }

    /**
     * Secondary constructor for Connection, primarily used for testing.
     *
     * @param requestBuilder   The request builder.
     * @param httpClientConfig The HTTP client configuration.
     * @param httpClient       The HTTP client.
     */
    Connection(RequestBuilder requestBuilder, HttpClientConfig httpClientConfig, HttpClient httpClient) {
        this.requestBuilder = requestBuilder;
        this.httpClientConfig = httpClientConfig;
        this.httpClient = httpClient;
    }

    /**
     * Creates and configures the HTTP client.
     *
     * @return An instance of HttpClient.
     */
    private HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(httpClientConfig.getConnectTimeout())
                .executor(Executors.newFixedThreadPool(httpClientConfig.getMaxConnections()))
                .build();
    }

    /**
     * Performs an asynchronous HTTP request to Fauna with the provided FQL query.
     *
     * @param fql The Fauna Query Language query to be executed.
     * @return A CompletableFuture that, when completed, will return the HttpResponse.
     */
    public CompletableFuture<HttpResponse<String>> performRequest(String fql) {
        HttpRequest request = requestBuilder.buildRequest(fql);
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Builder class for Connection. Follows the Builder Design Pattern.
     */
    public static class Builder {
        private FaunaConfig faunaConfig;
        private HttpClientConfig httpClientConfig;
        private JvmDriver jvmDriver;

        /**
         * Sets the FaunaConfig for the Connection.
         *
         * @param faunaConfig The configuration settings for Fauna.
         * @return The current Builder instance.
         */
        public Builder faunaConfig(FaunaConfig faunaConfig) {
            this.faunaConfig = faunaConfig;
            return this;
        }

        /**
         * Sets the HttpClientConfig for the Connection.
         *
         * @param httpClientConfig The HTTP client configuration.
         * @return The current Builder instance.
         */
        public Builder httpClientConfig(HttpClientConfig httpClientConfig) {
            this.httpClientConfig = httpClientConfig;
            return this;
        }

        /**
         * Sets the JvmDriver for the Connection.
         *
         * @param jvmDriver The JVM driver information.
         * @return The current Builder instance.
         */
        public Builder jvmDriver(JvmDriver jvmDriver) {
            this.jvmDriver = jvmDriver;
            return this;
        }

        /**
         * Builds and returns a new Connection instance.
         *
         * @return A new instance of Connection.
         */
        public Connection build() {
            return new Connection(this);
        }
    }

}
