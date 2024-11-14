package com.fauna.exception;

import java.text.MessageFormat;

/**
 * Exception representing errors in the client's response handling.
 * <p>
 * This exception is typically thrown when there is an issue with the response received from
 * Fauna, including unexpected status codes or other response-related errors.
 * Extends {@link ClientException} to provide information specific to response handling errors.
 */
public class ClientResponseException extends ClientException {

    /**
     * Constructs a new {@code ClientResponseException} with the specified detail message.
     *
     * @param message A {@code String} describing the reason for the client response error.
     */
    public ClientResponseException(final String message) {
        super(message);
    }

    /**
     * Constructs a new {@code ClientResponseException} with a formatted message
     * based on the provided status code and message.
     *
     * @param message    A {@code String} describing the reason for the client response error.
     * @param statusCode An {@code int} representing the HTTP status code received.
     * @return A formatted message string.
     */
    private static String buildMessage(final String message, final int statusCode) {
        return MessageFormat.format("ClientResponseException HTTP {0}: {1}",
                statusCode, message);
    }

    /**
     * Constructs a new {@code ClientResponseException} with the specified detail message, cause, and status code.
     *
     * @param message    A {@code String} describing the reason for the client response error.
     * @param exc        The underlying {@code Throwable} cause of the error.
     * @param statusCode An {@code int} representing the HTTP status code received.
     */
    public ClientResponseException(final String message, final Throwable exc,
                                   final int statusCode) {
        super(buildMessage(message, statusCode), exc);
    }

    /**
     * Constructs a new {@code ClientResponseException} with the specified detail message and cause.
     *
     * @param message A {@code String} describing the reason for the client response error.
     * @param cause   The underlying {@code Throwable} cause of the error.
     */
    public ClientResponseException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
