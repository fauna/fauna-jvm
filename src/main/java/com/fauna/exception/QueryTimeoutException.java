package com.fauna.exception;

import com.fauna.response.QueryFailure;

/**
 * Exception representing a timeout error encountered during query execution in Fauna.
 * <p>
 * This exception is thrown when a query fails to complete within the specified time limit,
 * indicating that the query timeout was exceeded.
 * Extends {@link ServiceException} to provide details specific to query timeout errors.
 */
public class QueryTimeoutException extends ServiceException {

    /**
     * Constructs a new {@code QueryTimeoutException} with the specified {@code QueryFailure} details.
     *
     * @param response The {@link QueryFailure} object containing details about the timeout error.
     */
    public QueryTimeoutException(final QueryFailure response) {
        super(response);
    }
}
