package com.fauna.exception;

import com.fauna.response.QueryFailure;

public class QueryTimeoutException extends ServiceException {
    public QueryTimeoutException(QueryFailure response) {
        super(response);
    }
}
