package com.fauna.client;

import com.fauna.common.connection.Connection;
import com.fauna.common.configuration.FaunaConfig;
import com.fauna.common.configuration.HttpClientConfig;
import com.fauna.exception.AuthenticationException;
import com.fauna.exception.InvalidQueryException;
import com.fauna.exception.ProtocolException;
import com.fauna.exception.ServiceErrorException;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class FaunaClient {

    private final Connection connection;

    public FaunaClient(FaunaConfig faunaConfig, HttpClientConfig httpClientConfig) {
        this.connection = Connection.builder()
                .faunaConfig(faunaConfig)
                .httpClientConfig(httpClientConfig)
                .build();
    }

    FaunaClient(FaunaConfig faunaConfig, HttpClientConfig httpClientConfig, Connection connection) {
        super();
        this.connection = connection;
    }

    public FaunaClient(FaunaConfig faunaConfig) {
        this(faunaConfig, HttpClientConfig.builder().build());
    }

    public CompletableFuture<HttpResponse<String>> query(String fql) {

        if (fql == null) {
            throw new IllegalArgumentException("The provided FQL query is null.");
        }

        return connection.performRequest(fql).thenApply(this::processResponse);
    }


    private HttpResponse<String> processResponse(HttpResponse<String> response) {
        int statusCode = response.statusCode();
        checkProtocol(response.body(), statusCode);
        if (statusCode > 399) {
            handleErrorResponse(response.body(), statusCode);
        }
        return response;
    }

    private void checkProtocol(String body, int statusCode) {
        if ((statusCode <= 399 && !body.contains("data")) || (statusCode > 399 && !body.contains("error"))) {
            throw new ProtocolException("Response is in an unknown format: " + body);
        }
    }

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

    private HttpResponse<String> handleException(Throwable ex) {
        throw new ServiceErrorException("Failed to process the request" + ex);
    }

}

