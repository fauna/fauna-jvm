package com.fauna.exception;

import com.fauna.response.QueryFailure;

import java.util.HashMap;
import java.util.Map;

public class AbortException extends ServiceException {
    @SuppressWarnings("rawtypes")
    private final Map<Class, Object> decoded = new HashMap<>();

    public AbortException(QueryFailure response) {
        super(response);
    }

    /**
     * Return the abort data as a top-level Object, mostly useful for debugging and other cases where you might
     * not know what to expect back.
     * @return
     */
    public Object getAbort() {
        return getAbort(Object.class);
    }

    /**
     * Return the abort data, decoded into the given class, or null if there was no abort data.
     * @param clazz The class to decode the abort data into.
     * @return      The abort data, or null.
     * @param <T>   The type of the abort data.
     */
    public <T> T getAbort(Class<T> clazz) {
        if (!decoded.containsKey(clazz)) {
            Object abortData = getResponse().getAbort(clazz).orElse(null);
            decoded.put(clazz, abortData);
        }
        //noinspection unchecked
        return (T) decoded.get(clazz);
    }
}
