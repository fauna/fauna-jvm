package com.fauna.exception;

/**
 * Represents a general exception for errors encountered within the Fauna client.
 * This exception serves as the base class for other specific exceptions in the drive.
 */
public class FaunaException extends RuntimeException {

    /**
     * Constructs a new {@code FaunaException} with the specified message.
     *
     * @param message A {@code String} describing the reason for the exception.
     */
    public FaunaException(final String message) {
        super(message);
    }

    /**
     * Constructs a new {@code FaunaException} with the specified detail message and cause.
     *
     * @param message A {@code String} describing the reason for the exception.
     * @param err     The underlying {@code Throwable} cause of the exception.
     */
    public FaunaException(final String message, final Throwable err) {
        super(message, err);
    }
}
