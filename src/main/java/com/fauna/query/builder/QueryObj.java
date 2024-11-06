package com.fauna.query.builder;

import java.util.Map;
import java.util.Objects;

/**
 * Represents an object fragment of a Fauna query. Object fragments allow for the evaluation of FQL statements
 * stored on the object. This class encapsulates an object that can be a variable in the query.
 */
public class QueryObj<E extends QueryFragment>
        extends QueryFragment<Map<String, E>> {

    public static <E extends QueryFragment> QueryObj of(Map<String, E> val) {
        //noinspection unchecked
        return new QueryObj(val);
    }

    private final Map<String, E> value;

    /**
     * Constructs a QueryObj with the specified value.
     *
     * @param value the value to encapsulate.
     */
    public QueryObj(Map<String, E> value) {
        this.value = value;
    }

    /**
     * Retrieves the encapsulated value of this fragment.
     *
     * @return the encapsulated object.
     */
    @Override
    public Map<String, E> get() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        QueryObj that = (QueryObj) o;

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
