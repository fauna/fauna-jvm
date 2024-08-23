package com.fauna.query.builder;

import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;
import com.fauna.codec.UTF8FaunaGenerator;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Represents a value fragment of a Fauna query.
 * This class encapsulates a value that can be a variable in the query.
 */
public class QueryArr<E,T> extends QueryFragment<T> {
    public static <E,T> QueryArr<E,T> of(T val) {
        return new QueryArr<>(val);
    }

    private final T value;

    /**
     * Constructs a ValueFragment with the specified value.
     *
     * @param value the value to encapsulate, which can be any object.
     */
    public QueryArr(T value) {
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

        QueryArr<E,T> that = (QueryArr<E,T>) o;

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