package com.fauna.exception;

/**
 * Exception representing errors related to client requests in Fauna.
 * <p>
 * This exception is thrown when there is an issue with the structure or content of a request
 * sent from the client, such as invalid parameters or improperly formatted data.
 * Extends {@link ClientException} to provide information specific to request-related errors.
 */
public class ClientRequestException extends ClientException {

    /**
     * Constructs a new {@code ClientRequestException} with the specified detail message.
     *
     * @param message A {@code String} describing the reason for the client request error.
     */
    public ClientRequestException(final String message) {
        super(message);
    }

    /**
     * Constructs a new {@code ClientRequestException} with the specified detail message and cause.
     *
     * @param message A {@code String} describing the reason for the client request error.
     * @param cause   The underlying {@code Throwable} cause of the error.
     */
    public ClientRequestException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
