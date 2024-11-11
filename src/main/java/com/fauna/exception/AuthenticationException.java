package com.fauna.exception;

import com.fauna.response.QueryFailure;

/**
 * Exception thrown when an authentication error occurs in Fauna.
 * This typically indicates an issue with the credentials used for accessing Fauna's services.
 * <p>
 * Extends {@link ServiceException} and provides access to detailed failure information through the
 * {@link QueryFailure} response.
 */
public class AuthenticationException extends ServiceException {

    /**
     * Constructs a new {@code AuthenticationException} with the specified {@code QueryFailure} response.
     *
     * @param response The {@code QueryFailure} object containing details about the authentication failure.
     */
    public AuthenticationException(final QueryFailure response) {
        super(response);
    }
}
