package com.fauna.exception;

import com.fauna.response.QueryFailure;

public class AuthorizationException extends ServiceException {
    public AuthorizationException(QueryFailure response) {
        super(response);
    }
}
