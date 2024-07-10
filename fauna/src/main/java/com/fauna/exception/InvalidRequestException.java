package com.fauna.exception;

import com.fauna.response.QueryFailure;

public class InvalidRequestException extends ServiceException {
    public InvalidRequestException(QueryFailure response) {
        super(response);
    }
}
