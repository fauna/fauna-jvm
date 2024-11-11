package com.fauna.exception;

import com.fauna.response.QueryFailure;

/**
 * Exception representing a throttling error in Fauna, indicating that a query exceeded
 * allowable usage limits.
 * <p>
 * This exception is thrown when Fauna restricts query execution due to usage limits.
 * It implements {@link RetryableException}
 * Extends {@link ServiceException} to provide details specific to throttling errors.
 */
public class ThrottlingException extends ServiceException implements RetryableException {

    /**
     * Constructs a new {@code ThrottlingException} with the specified {@code QueryFailure} details.
     *
     * @param response The {@link QueryFailure} object containing details about the throttling error.
     */
    public ThrottlingException(final QueryFailure response) {
        super(response);
    }
}
