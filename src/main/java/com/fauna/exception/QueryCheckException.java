package com.fauna.exception;

import com.fauna.response.QueryFailure;

/**
 * Exception representing a query validation error in Fauna.
 * <p>
 * This exception is thrown when a query fails one or more validation checks in Fauna,
 * indicating issues with the query's syntax, or other query validation prior to execution.
 * Extends {@link ServiceException} to provide information specific to query validation errors.
 */
public class QueryCheckException extends ServiceException {

    /**
     * Constructs a new {@code QueryCheckException} with the specified {@code QueryFailure} details.
     *
     * @param failure The {@link QueryFailure} object containing details about the validation failure.
     */
    public QueryCheckException(final QueryFailure failure) {
        super(failure);
    }
}
