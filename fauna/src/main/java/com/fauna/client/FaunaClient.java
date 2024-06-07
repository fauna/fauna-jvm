package com.fauna.client;

import com.fauna.common.configuration.JvmDriver;
import com.fauna.common.connection.Connection;
import com.fauna.common.configuration.FaunaConfig;
import com.fauna.common.configuration.HttpClientConfig;
import com.fauna.exception.AuthenticationException;
import com.fauna.exception.InvalidQueryException;
import com.fauna.exception.ProtocolException;
import com.fauna.exception.ServiceErrorException;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * FaunaClient is the main client for interacting with Fauna.
 * It provides functionality to send queries and receive responses.
 */
public class FaunaClient {

    private final Connection connection;

    /**
     * Constructs a new FaunaClient instance with the provided FaunaConfig and HttpClientConfig.
     *
     * @param faunaConfig      The Fauna configuration settings.
     * @param httpClientConfig The HTTP client configuration.
     */
    public FaunaClient(FaunaConfig faunaConfig, HttpClientConfig httpClientConfig) {
        this.connection = Connection.builder()
                .faunaConfig(faunaConfig)
                .httpClientConfig(httpClientConfig)
                .jvmDriver(JvmDriver.JAVA)
                .build();
    }

    /**
     * Secondary constructor for FaunaClient, primarily used for testing.
     *
     * @param faunaConfig      The Fauna configuration settings.
     * @param httpClientConfig The HTTP client configuration.
     * @param connection       The Connection instance to be used.
     */
    FaunaClient(FaunaConfig faunaConfig, HttpClientConfig httpClientConfig, Connection connection) {
        super();
        this.connection = connection;
    }

    /**
     * Constructs a new FaunaClient instance with the provided FaunaConfig.
     * It uses the default HTTP client configuration.
     *
     * @param faunaConfig The Fauna configuration settings.
     */
    public FaunaClient(FaunaConfig faunaConfig) {
        this(faunaConfig, HttpClientConfig.builder().build());
    }

    /**
     * Sends a Fauna Query Language (FQL) query to Fauna.
     *
     * @param fql The FQL query to be executed.
     * @return A CompletableFuture that, when completed, will return the HttpResponse.
     * @throws IllegalArgumentException If the provided FQL query is null.
     */
    public CompletableFuture<HttpResponse<String>> query(String fql) {

        if (fql == null) {
            throw new IllegalArgumentException("The provided FQL query is null.");
        }

        return connection.performRequest(fql).thenApply(this::processResponse);
    }

    /**
     * Processes the HTTP response from Fauna, checking for errors and ensuring protocol compliance.
     *
     * @param response The HTTP response from Fauna.
     * @return The original HttpResponse if no errors were detected.
     * @throws ProtocolException       If the response is in an unknown format.
     * @throws AuthenticationException If there was an authentication error.
     * @throws InvalidQueryException   If the query was invalid.
     * @throws ServiceErrorException   For other types of errors.
     */
    private HttpResponse<String> processResponse(HttpResponse<String> response) {
        int statusCode = response.statusCode();
        checkProtocol(response.body(), statusCode);
        if (statusCode > 399) {
            handleErrorResponse(response.body(), statusCode);
        }
        return response;
    }

    /**
     * Checks if the response body is in the expected format based on the status code.
     *
     * @param body       The response body.
     * @param statusCode The HTTP status code.
     * @throws ProtocolException If the response is in an unknown format.
     */
    private void checkProtocol(String body, int statusCode) {
        if ((statusCode <= 399 && !body.contains("data")) || (statusCode > 399 && !body.contains("error"))) {
            throw new ProtocolException("Response is in an unknown format: " + body);
        }
    }

    /**
     * Handles errors based on the HTTP status code and response body.
     *
     * @param body       The response body.
     * @param statusCode The HTTP status code.
     * @throws AuthenticationException If there was an authentication error.
     * @throws InvalidQueryException   If the query was invalid.
     * @throws ServiceErrorException   For other types of errors.
     */
    private void handleErrorResponse(String body, int statusCode) {

        //TODO: code and message from body

        switch (statusCode) {
            case 400:
                if ("invalid_query".equals("code")) {
                    throw new InvalidQueryException("message");
                }
                break;
            case 401:
                throw new AuthenticationException("message");

            default:
                throw new ServiceErrorException("message");
        }
    }

    /**
     * Handles exceptions during the HTTP request processing.
     *
     * @param ex The exception that was thrown.
     * @return Does not return anything as it always throws an exception.
     * @throws ServiceErrorException Wrapping the original exception, indicating a failure to process the request.
     */
    private HttpResponse<String> handleException(Throwable ex) {
        throw new ServiceErrorException("Failed to process the request" + ex);
    }

}

