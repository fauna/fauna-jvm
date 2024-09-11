package com.fauna.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fauna.response.wire.QueryResponseWire;
import com.fauna.response.QueryFailure;

import java.io.IOException;

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
     * @param statusCode        The HTTP status code.
     * @param response          The decoded response.
     * @throws AbortException
     * @throws AuthenticationException
     * @throws AuthorizationException
     * @throws ConstraintFailureException
     * @throws ContendedTransactionException
     * @throws InvalidRequestException
     * @throws ProtocolException
     * @throws QueryCheckException
     * @throws QueryRuntimeException
     * @throws QueryTimeoutException
     * @throws ThrottlingException
     *
     */
    public static void handleErrorResponse(int statusCode, QueryResponseWire response, String body) {
        QueryFailure failure = new QueryFailure(statusCode, response);
        handleQueryFailure(statusCode, failure);
        throw new ProtocolException(statusCode, body);
    }

    /**
     * Handles errors based on the HTTP status code and error code.
     *
     * @param statusCode    The HTTP status code.
     * @param failure       The decoded QueryFailure body.
     * @throws AbortException
     * @throws AuthenticationException
     * @throws AuthorizationException
     * @throws ConstraintFailureException
     * @throws ContendedTransactionException
     * @throws InvalidRequestException
     * @throws QueryCheckException
     * @throws QueryRuntimeException
     * @throws QueryTimeoutException
     * @throws ThrottlingException
     *
     */
    public static void handleQueryFailure(int statusCode, QueryFailure failure) {
        switch (statusCode) {
            case HTTP_BAD_REQUEST:
                switch (failure.getErrorCode()) {
                    case INVALID_QUERY: throw new QueryCheckException(failure);
                    case LIMIT_EXCEEDED: throw new ThrottlingException(failure);
                    case INVALID_REQUEST: throw new InvalidRequestException(failure);
                    case ABORT: throw new AbortException(failure);
                    case CONSTRAINT_FAILURE: throw new ConstraintFailureException(failure);
                    // There are ~30 more error codes that map to a QueryRuntimeException.
                    // By using a default here, one of them is not strictly required. But we
                    // _do_ require a valid JSON body that can be decoded to a
                    // QueryFailure. Defaulting here also slightly future-proofs this client
                    // because Fauna can throw 400s with new error codes.
                    default: throw new QueryRuntimeException(failure);
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
            case 429:
                // 400 (above), or 429 with "limit_exceeded" -> ThrottlingException.
                if (LIMIT_EXCEEDED.equals(failure.getErrorCode())) {
                    throw new ThrottlingException(failure);
                }
            case 440:
            case HTTP_UNAVAILABLE:
                // 400 or 503 with "time_out" -> QueryTimeoutException.
                if (TIME_OUT.equals(failure.getErrorCode())) {
                    throw new QueryTimeoutException(failure);
                }
            case HTTP_INTERNAL_ERROR:
                if (INTERNAL_ERROR.equals(failure.getErrorCode())) {
                    throw new ServiceInternalException(failure);
                }
        }
    }
}
