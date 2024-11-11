package com.fauna.exception;

import com.fauna.response.QueryFailure;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;

/**
 * Provides error handling based on <a href="https://docs.fauna.com/fauna/current/reference/http/reference/errors/#error-codes">error codes</a> and HTTP status codes returned by Fauna.
 * <p>
 * The {@code ErrorHandler} class contains a static method to manage various error scenarios
 * by analyzing the HTTP status code and specific error codes, mapping them to relevant exceptions.
 */
public final class ErrorHandler {
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
    private static final int HTTP_LIMIT_EXCEEDED = 429;
    private static final int HTTP_TIME_OUT = 440;

    private ErrorHandler() {
    }

    /**
     * Handles errors based on the HTTP status code and error code returned by Fauna.
     *
     * @param statusCode The HTTP status code received from Fauna.
     * @param failure    The {@link QueryFailure} object containing details about the failure.
     * @throws AbortException                Thrown if the transaction was aborted.
     * @throws AuthenticationException       Thrown if authentication credentials are invalid or missing.
     * @throws AuthorizationException        Thrown if authorization credentials are invalid or insufficient.
     * @throws ConstraintFailureException    Thrown if the transaction failed a database constraint check.
     * @throws ContendedTransactionException Thrown if too much contention occurred during a transaction.
     * @throws InvalidRequestException       Thrown if the request body does not conform to API specifications.
     * @throws QueryCheckException           Thrown if the query failed validation checks.
     * @throws QueryRuntimeException         Thrown if the query encountered a runtime error.
     * @throws QueryTimeoutException         Thrown if the query exceeded the specified timeout.
     * @throws ServiceInternalException      Thrown if an unexpected server error occurred.
     * @throws ThrottlingException           Thrown if the query exceeded capacity limits.
     */
    public static void handleQueryFailure(
            final int statusCode,
            final QueryFailure failure) {
        switch (statusCode) {
            case HTTP_BAD_REQUEST:
                switch (failure.getErrorCode()) {
                    case INVALID_QUERY:
                        throw new QueryCheckException(failure);
                    case LIMIT_EXCEEDED:
                        throw new ThrottlingException(failure);
                    case INVALID_REQUEST:
                        throw new InvalidRequestException(failure);
                    case ABORT:
                        throw new AbortException(failure);
                    case CONSTRAINT_FAILURE:
                        throw new ConstraintFailureException(failure);
                    default:
                        throw new QueryRuntimeException(failure);
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
            case HTTP_LIMIT_EXCEEDED:
                if (LIMIT_EXCEEDED.equals(failure.getErrorCode())) {
                    throw new ThrottlingException(failure);
                }
            case HTTP_TIME_OUT:
            case HTTP_UNAVAILABLE:
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
