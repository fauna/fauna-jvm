package com.fauna.exception;

import com.fauna.response.QueryFailure;

/**
 * Exception representing an invalid query request.
 * <p>
 * This exception is thrown when a request sent to Fauna does not conform to the API specifications,
 * typically due to malformed data, incorrect parameters, or other request-related issues.
 * Extends {@link ServiceException} to provide specific details about the invalid request.
 */
public class InvalidRequestException extends ServiceException {

    /**
     * Constructs a new {@code InvalidRequestException} with the specified {@code QueryFailure} response.
     *
     * @param response The {@code QueryFailure} object containing details about the invalid request.
     */
    public InvalidRequestException(final QueryFailure response) {
        super(response);
    }
}
