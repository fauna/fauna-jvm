package com.fauna.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fauna.response.QueryFailure;
import com.fauna.response.QueryStats;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;

public class ErrorHandler {
     private static final String INVALID_QUERY = "invalid_query";
     private static final String LIMIT_EXCEEDED = "limit_exceeded";
     private static final String INVALID_REQUEST = "invalid_request";
     private static final String ABORT = "abort";
     private static final String CONSTRAINT_FAILURE = "constraint_failure";
     private static final String UNAUTHORIZED = "unauthorized";
     private static final String FORBIDDEN = "forbidden";
     private static final String CONTENDED_TRANSACTION = "contended_transaction";
     private static final String TIME_OUT = "time_out";
     private static final String INTERNAL_ERROR = "internal_error";

    /**
     * Handles errors based on the HTTP status code and response body.
     *
     * @param statusCode The HTTP status code.
     * @throws AuthenticationException If there was an authentication error.
     * @throws FaunaInvalidQuery   If the query was invalid.
     * @throws ServiceException   For other types of errors.
     */
    public static void handleErrorResponse(int statusCode, JsonNode json, QueryStats stats) throws JsonProcessingException {
        QueryFailure failure = new QueryFailure(statusCode, json, stats);

        switch (statusCode) {
            case HTTP_BAD_REQUEST:
                switch (failure.getErrorCode()) {
                    case INVALID_QUERY: throw new QueryCheckException(failure);
                    case LIMIT_EXCEEDED: throw new ThrottlingException(failure);
                    case INVALID_REQUEST: throw new InvalidRequestException(failure);
                    case ABORT: throw new AbortException(failure);
                    case CONSTRAINT_FAILURE: throw new ConstraintFailureException(failure);
                    default: throw new QueryException(failure);
                }
            case HTTP_UNAUTHORIZED:
                if (UNAUTHORIZED.equals(failure.getErrorCode())) {
                    throw new AuthenticationException(failure);
                }
            case HTTP_FORBIDDEN:
                if (FORBIDDEN.equals(failure.getErrorCode())) {
                    throw new AuthorizationException(failure);
                }
            case HTTP_CONFLICT:
                if (CONTENDED_TRANSACTION.equals(failure.getErrorCode())) {
                    throw new ContendedTransactionException(failure);
                }
            // TODO: Will 429 from firewall, routers etc have the correct response body?
            case 429: throw new ThrottlingException(failure);
            case 440:
            case HTTP_UNAVAILABLE:
                if (TIME_OUT.equals(failure.getErrorCode())) {
                    throw new QueryTimeoutException(failure);
                }
            case HTTP_INTERNAL_ERROR:
                if (INTERNAL_ERROR.equals(failure.getErrorCode())) {
                    throw new ServiceInternalException(failure);
                }
        }
        throw new QueryException(failure);
    }
}
