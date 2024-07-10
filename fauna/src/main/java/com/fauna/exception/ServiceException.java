package com.fauna.exception;

import com.fauna.response.QueryFailure;

public class ServiceException extends FaunaException {
    private final QueryFailure response;

    public ServiceException(QueryFailure response) {
        super(response.getMessage());
        this.response = response;
    }

    public QueryFailure getResponse() {
        return this.response;
    }

}
