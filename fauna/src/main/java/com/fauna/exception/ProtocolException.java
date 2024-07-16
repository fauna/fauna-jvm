package com.fauna.exception;

import java.text.MessageFormat;

public class ProtocolException extends FaunaException {
    private int statusCode;
    private String body;

    public ProtocolException(Throwable exc, int statusCode, String body) {
        super(MessageFormat.format("Protocol Exception %d: %s", statusCode, body), exc);
        this.statusCode = statusCode;
        this.body = body;
    }

    public ProtocolException(int statusCode, String body) {
        super(MessageFormat.format("Protocol Exception %d: %s", statusCode, body));
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
