package com.fauna.exception;

import com.fauna.response.QueryFailure;

import java.util.HashMap;
import java.util.Map;

/**
 * An exception that represents an aborted query in Fauna.
 * This exception extends {@link ServiceException} and includes methods to retrieve
 * the abort data.
 */
public class AbortException extends ServiceException {
    @SuppressWarnings("rawtypes")
    private final Map<Class, Object> decoded = new HashMap<>();

    /**
     * Constructs a new {@code AbortException} with the specified {@link QueryFailure} response.
     *
     * @param response The {@code QueryFailure} object containing details about the aborted query.
     */
    public AbortException(final QueryFailure response) {
        super(response);
    }

    /**
     * Returns the abort data as a top-level {@code Object}. This is primarily useful for debugging
     * or situations where the type of abort data may be unknown.
     *
     * @return An {@code Object} containing the abort data, or {@code null} if no data is present.
     */
    public Object getAbort() {
        return getAbort(Object.class);
    }

    /**
     * Returns the abort data decoded into the specified class, or {@code null} if there is no abort data.
     * The abort data is cached upon retrieval to avoid redundant decoding.
     *
     * @param clazz The {@code Class} to decode the abort data into.
     * @param <T>   The type of the abort data.
     * @return The decoded abort data of type {@code T}, or {@code null} if no data is available.
     */
    public <T> T getAbort(final Class<T> clazz) {
        if (!decoded.containsKey(clazz)) {
            Object abortData = getResponse().getAbort(clazz).orElse(null);
            decoded.put(clazz, abortData);
        }
        //noinspection unchecked
        return (T) decoded.get(clazz);
    }
}
