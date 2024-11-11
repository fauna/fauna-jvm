package com.fauna.exception;

import com.fauna.response.QueryFailure;

/**
 * Exception thrown when an authorization error occurs in Fauna.
 * This typically indicates that the authenticated user does not have permission
 * to perform the requested action.
 * <p>
 * Extends {@link ServiceException} and provides access to detailed failure information through
 * the {@link QueryFailure} response.
 */
public class AuthorizationException extends ServiceException {

    /**
     * Constructs a new {@code AuthorizationException} with the specified {@code QueryFailure} response.
     *
     * @param response The {@code QueryFailure} object containing details about the authorization failure.
     */
    public AuthorizationException(final QueryFailure response) {
        super(response);
    }
}
