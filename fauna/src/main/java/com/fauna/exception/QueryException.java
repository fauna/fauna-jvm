package com.fauna.exception;

import com.fauna.response.QueryFailure;

public class QueryException extends ServiceException {
    public QueryException(QueryFailure response) {
        super(response);
    }
}
