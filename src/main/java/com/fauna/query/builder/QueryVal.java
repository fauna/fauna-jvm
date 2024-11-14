package com.fauna.query.builder;

import java.util.Objects;

/**
 * Represents a value fragment of a Fauna query.
 * This class encapsulates a value that can be a variable in the query, such as a literal value or a reference.
 * The value can be any object type and will be substituted into the query at runtime.
 *
 * @param <T> The type of the value in the fragment, which can be any object.
 */
public final class QueryVal<T> extends QueryFragment<T> {

    private final T value;

    /**
     * Constructs a QueryVal with the specified value.
     *
     * @param value the value to encapsulate, which can be any object.
     *              It can represent a literal value or a reference to be used in the query.
     */
    public QueryVal(final T value) {
        this.value = value;
    }

    /**
     * Retrieves the encapsulated value of this fragment.
     *
     * @return the value contained within this fragment.
     */
    @Override
    public T get() {
        return value;
    }

    /**
     * Compares this QueryVal to another object for equality.
     * Two QueryVal objects are considered equal if their encapsulated values are equal.
     *
     * @param o the object to compare to.
     * @return {@code true} if this QueryVal is equal to the other object, otherwise {@code false}.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        @SuppressWarnings("unchecked")
        QueryVal<T> that = (QueryVal<T>) o;

        return Objects.equals(value, that.value);
    }

    /**
     * Returns the hash code for this QueryVal.
     * The hash code is computed based on the encapsulated value.
     *
     * @return the hash code of this QueryVal.
     */
    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    /**
     * Retrieves the value wrapped inside this fragment.
     *
     * @return the value contained in the QueryVal fragment.
     */
    public Object getValue() {
        return this.value;
    }

}
