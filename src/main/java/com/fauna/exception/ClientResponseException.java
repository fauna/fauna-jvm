package com.fauna.exception;

import java.text.MessageFormat;

public class ClientResponseException extends ClientException {

    public ClientResponseException(String message) {
        super(message);
    }

    private static String buildMessage(String message, int statusCode) {
        return MessageFormat.format("ClientResponseException HTTP {0}: {1}",
                statusCode, message);
    }

    public ClientResponseException(String message, Throwable exc,
                                   int statusCode) {
        super(buildMessage(message, statusCode), exc);
    }

    public ClientResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}