package com.fauna.exception;

public class ServiceErrorException  extends RuntimeException {
    public ServiceErrorException(String message) {
        super(message);
    }
}
