package com.fauna.exception;

public class ServiceErrorException  extends FaunaException {
    public ServiceErrorException(String message) {
        super(message);
    }
}
