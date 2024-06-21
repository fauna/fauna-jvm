package com.fauna.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fauna.common.configuration.FaunaConfig;
import com.fauna.exception.AuthenticationException;
import com.fauna.exception.FaunaException;
import com.fauna.exception.InvalidQueryException;
import com.fauna.exception.ProtocolException;
import com.fauna.exception.ServiceErrorException;
import com.fauna.mapping.MappingContext;
import com.fauna.query.builder.Query;
import com.fauna.response.QueryResponse;
import com.fauna.serialization.Deserializer;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * FaunaClient is the main client for interacting with Fauna.
 * It provides functionality to send queries and receive responses.
 */
public class FaunaClient {

    // private final FaunaConfig config;
    private final HttpClient httpClient;
    private final RequestBuilder requestBuilder;
    private final ObjectMapper mapper;

    /**
     * Construct a new FaunaClient instance with the provided FaunaConfig and HttpClient. This allows
     * complete control over HTTP Configuration, like timeouts, thread pool size, and so-on.
     *
     *   * Note that FaunaConfig.queryTimeout will be ignored if using this method directly.
     *
     * @param faunaConfig The Fauna configuration settings.
     * @param httpClient  A Java HTTP client instance.
     */
    public FaunaClient(FaunaConfig faunaConfig,
                       HttpClient httpClient) {
        this.httpClient = httpClient;
        this.mapper = new ObjectMapper();
        if (Objects.isNull(faunaConfig)) {
            throw new IllegalArgumentException("FaunaConfig cannot be null.");
        } else {
            this.requestBuilder = new RequestBuilder(faunaConfig);
        }
        if (Objects.isNull(httpClient)) {
            throw new IllegalArgumentException("HttpClient cannot be null.");
        }
    }

    /**
     * Construct a new FaunaClient instance with the provided FaunaConfig, uses a default HTTP client configuration.
     *
     * @param faunaConfig The Fauna configuration settings.
     */
    public FaunaClient(FaunaConfig faunaConfig) {
        this(faunaConfig, HttpClient.newBuilder().build());
    }

    /**
     * Construct a new FaunaClient instance with default configuration.
     */
    public FaunaClient() {
        this(FaunaConfig.builder().build());
    }

    /**
     * Sends a Fauna Query Language (FQL) query to Fauna.
     *
     * @param fql The FQL query to be executed.
     * @return QuerySuccess
     * @throws FaunaException If the provided FQL query is null.
     */
    public CompletableFuture<QueryResponse> asyncQuery(Query fql) throws FaunaException {
        if (Objects.isNull(fql)) {
            throw new IllegalArgumentException("The provided FQL query is null.");
        }
        try {
            HttpRequest request = requestBuilder.buildRequest(mapper.writeValueAsString(fql));
        } catch (JsonProcessingException exc) {
            throw new FaunaException("TODO proper exception handling.");
        }


        return CompletableFuture.supplyAsync(() -> QueryResponse.getFromResponseBody(new MappingContext(), Deserializer.DYNAMIC, 200, "{\"hello\"}"));

    }

    public QueryResponse query(Query fql) throws FaunaException {
        if (Objects.isNull(fql)) {
            throw new IllegalArgumentException("The provided FQL query is null.");
        }
        try {
            HttpRequest request = requestBuilder.buildRequest(mapper.writeValueAsString(fql));
            HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return QueryResponse.getFromResponseBody(new MappingContext(), Deserializer.DYNAMIC,
                    response.statusCode(), response.body());
        } catch (Exception exc) {
            throw new FaunaException("TODO proper exception handling");
        }

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

