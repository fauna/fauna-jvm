package com.fauna.exception;

import com.fauna.response.QueryFailure;

import java.io.IOException;

public class AbortException extends ServiceException {

    public AbortException(QueryFailure response) {
        super(response);
    }

    public Object getAbort() throws IOException {
        return getAbort(Object.class);
    }

    public <T> T getAbort(Class<T> clazz) {
        return getResponse().getAbort(clazz).orElseThrow();
    }
}
