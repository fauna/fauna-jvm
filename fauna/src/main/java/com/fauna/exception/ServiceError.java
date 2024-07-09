package com.fauna.exception;

public class ServiceError extends FaunaException {
    public ServiceError(String message) {
        super(message);
    }
}
