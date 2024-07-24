package com.fauna.exception;

import com.fauna.response.QueryFailure;

public class QueryRuntimeException extends ServiceException {
    public QueryRuntimeException(QueryFailure response) {
        super(response);
    }
}
