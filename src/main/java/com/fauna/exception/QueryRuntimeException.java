package com.fauna.exception;

import com.fauna.response.QueryFailure;

/**
 * Exception representing a runtime error encountered during query execution in Fauna.
 * <p>
 * This exception is thrown when a query fails due to a runtime error.
 * Extends {@link ServiceException} to provide details specific to runtime query errors.
 */
public class QueryRuntimeException extends ServiceException {

    /**
     * Constructs a new {@code QueryRuntimeException} with the specified {@code QueryFailure} details.
     *
     * @param response The {@link QueryFailure} object containing details about the runtime error.
     */
    public QueryRuntimeException(final QueryFailure response) {
        super(response);
    }
}
