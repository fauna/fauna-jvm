package com.fauna.exception;

import com.fauna.response.QueryFailure;

import java.text.MessageFormat;
import java.util.Optional;

public class ProtocolException extends FaunaException {
    private final int statusCode;
    private QueryFailure queryFailure;
    private String body;

    private static String buildMessage(int statusCode) {
        return MessageFormat.format("ProtocolException HTTP {0}", statusCode);
    }

    public ProtocolException(int statusCode, QueryFailure failure) {
        super(MessageFormat.format("ProtocolException HTTP {0}", statusCode));
        this.statusCode = statusCode;
        this.queryFailure = failure;
    }

    public ProtocolException(int statusCode, String body) {
        super(buildMessage(statusCode));
        this.statusCode = statusCode;
        this.body = body;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public String getBody() {
        return this.body;
    }

    public Optional<QueryFailure> getQueryFailure() {
        return Optional.ofNullable(this.queryFailure);
    }
}
