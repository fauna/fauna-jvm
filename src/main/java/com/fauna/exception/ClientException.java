package com.fauna.exception;

/**
 * Exception representing client-side errors in Fauna.
 * <p>
 * This exception is typically thrown when there is an issue with client configuration,
 * request formation, or any other client-specific error that does not originate from Fauna.
 * Extends {@link FaunaException} to provide detailed information about the error.
 */
public class ClientException extends FaunaException {

    /**
     * Constructs a new {@code ClientException} with the specified detail message.
     *
     * @param message A {@code String} describing the reason for the client error.
     */
    public ClientException(final String message) {
        super(message);
    }

    /**
     * Constructs a new {@code ClientException} with the specified detail message and cause.
     *
     * @param message A {@code String} describing the reason for the client error.
     * @param cause   The underlying {@code Throwable} cause of the error.
     */
    public ClientException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
