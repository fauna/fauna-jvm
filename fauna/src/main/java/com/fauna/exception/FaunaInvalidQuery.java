package com.fauna.exception;

import com.fauna.response.QueryFailure;

public class FaunaInvalidQuery extends ServiceException {
    public FaunaInvalidQuery(QueryFailure response) {
        super(response);
    }
}
