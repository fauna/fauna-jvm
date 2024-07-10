package com.fauna.exception;

import com.fauna.response.QueryFailure;

public class ServiceInternalException extends ServiceException {
    public ServiceInternalException(QueryFailure response) {
        super(response);
    }
}
