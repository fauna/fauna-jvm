package com.fauna.query.builder;

import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;
import com.fauna.codec.UTF8FaunaGenerator;

import java.io.IOException;
import java.util.Objects;

/**
 * Represents an object fragment of a Fauna query. Object fragments allow for the evaluation of FQL statements
 * stored on the object. This class encapsulates an object that can be a variable in the query.
 */
public class QueryObj<T> extends QueryFragment<T> {

    public static <T> QueryObj<T> of(T val) {
        return new QueryObj<>(val);
    }

    private final T value;

    /**
     * Constructs a QueryObj with the specified value.
     *
     * @param value the value to encapsulate, which can be any object.
     */
    public QueryObj(T value) {
        this.value = value;
    }

    /**
     * Retrieves the encapsulated value of this fragment.
     *
     * @return the encapsulated object.
     */
    @Override
    public T get() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueryObj<T> that = (QueryObj<T>) o;

        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    public Object getValue() {
        return this.value;
    }
}
