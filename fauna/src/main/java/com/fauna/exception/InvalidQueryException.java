package com.fauna.exception;

public class InvalidQueryException extends FaunaException {
    public InvalidQueryException(String message) {
        super(message);
    }
}
