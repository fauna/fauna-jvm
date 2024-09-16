package com.fauna.exception;

import com.fauna.response.QueryFailure;

import java.util.HashMap;
import java.util.Map;

public class AbortException extends ServiceException {
    Map<Class, Object> decoded = new HashMap<>();

    public AbortException(QueryFailure response) {
        super(response);
    }

    public Object getAbort() {
        return getAbort(Object.class);
    }

    public <T> T getAbort(Class<T> clazz) {
        if (!decoded.containsKey(clazz)) {
            Object abortData = getResponse().getAbort(clazz).orElseThrow();
            decoded.put(clazz, abortData);
        }
        return (T) decoded.get(clazz);
    }
}
