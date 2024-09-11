package com.fauna.exception;

public class ClientRequestException extends ClientException {

    public ClientRequestException(String message) {
        super(message);
    }

    public ClientRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}