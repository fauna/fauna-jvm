package com.fauna.exception;

import java.io.IOException;

/**
 * Exception representing errors encountered during encoding or decoding operations.
 * <p>
 * This exception is typically thrown when an encoding or decoding error occurs within the Fauna
 * client, such as an {@link IOException} while reading or writing data.
 * Extends {@link FaunaException} to provide detailed information about codec-related errors.
 */
public class CodecException extends FaunaException {

    /**
     * Constructs a new {@code CodecException} with the specified detail message.
     *
     * @param message A {@code String} describing the reason for the codec error.
     */
    public CodecException(final String message) {
        super(message);
    }

    /**
     * Constructs a new {@code CodecException} with the specified detail message and cause.
     *
     * @param message A {@code String} describing the reason for the codec error.
     * @param cause   The underlying {@code Throwable} cause of the error.
     */
    public CodecException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new {@code CodecException} specifically for decoding {@link IOException}s.
     *
     * @param exc The {@code IOException} encountered during decoding.
     * @return A {@code CodecException} describing the decoding error.
     */
    public static CodecException decodingIOException(final IOException exc) {
        return new CodecException("IOException while decoding.", exc);
    }

    /**
     * Creates a new {@code CodecException} specifically for encoding {@link IOException}s.
     *
     * @param exc The {@code IOException} encountered during encoding.
     * @return A {@code CodecException} describing the encoding error.
     */
    public static CodecException encodingIOException(final IOException exc) {
        return new CodecException("IOException while encoding.", exc);
    }
}
