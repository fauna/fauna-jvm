package com.fauna.exception;

import com.fauna.response.QueryFailure;

public class ThrottlingException extends ServiceException implements RetryableException {
    public ThrottlingException(QueryFailure response) {
        super(response);
    }
}
