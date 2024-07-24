package com.fauna.exception;

import com.fauna.response.QueryFailure;

public class QueryCheckException extends ServiceException {
    public QueryCheckException(QueryFailure failure) {
        super(failure);
    }
}
