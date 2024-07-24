package com.fauna.exception;

import com.fauna.response.QueryFailure;

public class AuthenticationException extends ServiceException {
    public AuthenticationException(QueryFailure response) {
        super(response);
    }
}
