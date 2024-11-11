package com.fauna.exception;

import com.fauna.response.QueryFailure;

/**
 * Exception representing an unexpected internal server error in Fauna.
 * <p>
 * This exception is thrown when Fauna encounters an unexpected internal error that prevents
 * it from completing a request, typically indicating a server-side issue.
 * Extends {@link ServiceException} to provide details specific to internal server errors.
 */
public class ServiceInternalException extends ServiceException {

    /**
     * Constructs a new {@code ServiceInternalException} with the specified {@code QueryFailure} details.
     *
     * @param response The {@link QueryFailure} object containing details about the internal server error.
     */
    public ServiceInternalException(final QueryFailure response) {
        super(response);
    }
}
