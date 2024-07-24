package com.fauna.exception;

import java.text.MessageFormat;

public class ProtocolException extends FaunaException {
    private final int statusCode;
    private final String body;

    private static String buildMessage(int statusCode, String body) {
        return MessageFormat.format("ProtocolException HTTP {0} with body: {1}", statusCode, body);
    }

    public ProtocolException(Throwable exc, int statusCode, String body) {
        super(buildMessage(statusCode, body), exc);
        this.statusCode = statusCode;
        this.body = body;
    }

    public ProtocolException(int statusCode, String body) {
        super(buildMessage(statusCode, body));
        this.statusCode = statusCode;
        this.body = body;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public String getBody() {
        return this.body;
    }
}
