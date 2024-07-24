package com.fauna.exception;

public class ClientException extends FaunaException {

    public ClientException(String message) {
        super(message);
    }

    public ClientException(String message, Throwable cause) {
        super(message, cause);
    }
}