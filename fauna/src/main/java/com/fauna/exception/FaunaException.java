package com.fauna.exception;

public class FaunaException extends RuntimeException {

    public FaunaException(String message) {
        super(message);
    }

    public FaunaException(String message, Throwable cause) {
        super(message, cause);
    }

    public boolean retryable() {
        return false;
    }
}
