package com.fauna.exception;

import com.fauna.response.QueryFailure;


import java.util.concurrent.ExecutionException;

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
     * Handles errors based on the HTTP status code and error code.
     *
     * @param statusCode                    The HTTP status code.
     * @param failure                       The decoded QueryFailure body.
     * @throws AbortException               The transaction was aborted.
     * @throws AuthenticationException      Invalid or missing authentication token.
     * @throws AuthorizationException       Invalid or missing authentication token.
     * @throws ConstraintFailureException   The transaction failed a check constraint.
     * @throws ContendedTransactionException Too much contention occurred on a document while executing a query.
     * @throws InvalidRequestException      The request body does not conform to the API specification.
     * @throws QueryCheckException          The query failed one or more validation checks.
     * @throws QueryRuntimeException        The query failed due to a runtime error.
     * @throws QueryTimeoutException        The client specified timeout was exceeded.
     * @throws ServiceInternalException     An unexpected server error occured.
     * @throws ThrottlingException          The query exceeded some capacity limit.
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
