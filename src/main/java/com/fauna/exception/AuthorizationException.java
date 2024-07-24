package com.fauna.exception;

import com.fauna.response.QueryFailure;

public class AuthorizationException extends ServiceException {
    public static final String ERROR_CODE = "forbidden";
    public AuthorizationException(QueryFailure response) {
        super(response);
    }
}
