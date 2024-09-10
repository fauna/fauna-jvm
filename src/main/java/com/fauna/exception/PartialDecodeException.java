package com.fauna.exception;

public class PartialDecodeException extends ClientException {

    public PartialDecodeException(String message) {
        super(message);
    }

    public PartialDecodeException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
