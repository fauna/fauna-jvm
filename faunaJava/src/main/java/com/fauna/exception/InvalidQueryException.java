package com.fauna.exception;

public class InvalidQueryException extends RuntimeException{
    public InvalidQueryException(String message) {
        super(message);
    }
}
