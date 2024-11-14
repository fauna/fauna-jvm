package com.fauna.exception;

import com.fauna.response.QueryFailure;

import java.text.MessageFormat;
import java.util.Optional;

/**
 * Exception representing protocol-level errors in communication with Fauna.
 * <p>
 * This exception is typically thrown when there is an unexpected shape is received on response..
 * Extends {@link FaunaException} to provide details specific to protocol errors.
 */
public class ProtocolException extends FaunaException {

    private final int statusCode;
    private final QueryFailure queryFailure;
    private final String body;

    /**
     * Constructs a {@code ProtocolException} with the specified HTTP status code and {@code QueryFailure} details.
     *
     * @param statusCode The HTTP status code received.
     * @param failure    The {@link QueryFailure} object containing details about the protocol failure.
     */
    public ProtocolException(final int statusCode, final QueryFailure failure) {
        super(MessageFormat.format("ProtocolException HTTP {0}", statusCode));
        this.statusCode = statusCode;
        this.queryFailure = failure;
        this.body = null;
    }

    /**
     * Constructs a {@code ProtocolException} with the specified HTTP status code and response body.
     *
     * @param statusCode The HTTP status code received.
     * @param body       A {@code String} containing the response body associated with the failure.
     */
    public ProtocolException(final int statusCode, final String body) {
        super(buildMessage(statusCode));
        this.statusCode = statusCode;
        this.body = body;
        this.queryFailure = null;
    }

    /**
     * Builds a formatted error message based on the HTTP status code.
     *
     * @param statusCode The HTTP status code received.
     * @return A formatted {@code String} message for the protocol error.
     */
    private static String buildMessage(final int statusCode) {
        return MessageFormat.format("ProtocolException HTTP {0}", statusCode);
    }

    /**
     * Retrieves the HTTP status code associated with this protocol error.
     *
     * @return An {@code int} representing the HTTP status code.
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * Retrieves the response body associated with this protocol error, if available.
     *
     * @return A {@code String} containing the response body, or {@code null} if the body is unavailable.
     */
    public String getBody() {
        return this.body;
    }

    /**
     * Retrieves the {@link QueryFailure} details associated with this protocol error, if available.
     *
     * @return An {@code Optional<QueryFailure>} containing the failure details, or {@code Optional.empty()} if not
     * present.
     */
    public Optional<QueryFailure> getQueryFailure() {
        return Optional.ofNullable(this.queryFailure);
    }
}
