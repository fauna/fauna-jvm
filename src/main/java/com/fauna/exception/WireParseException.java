package com.fauna.exception;

public class WireParseException extends FaunaException {

    public WireParseException(String message) {
        super(message);
    }

    public WireParseException(String message, Throwable cause) {
        super(message, cause);
    }
}