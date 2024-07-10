package com.fauna.exception;

import com.fauna.response.QueryFailure;

public class AbortException extends ServiceException {
    public AbortException(QueryFailure response) {
        super(response);
    }
}
