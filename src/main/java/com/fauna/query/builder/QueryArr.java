package com.fauna.query.builder;

import java.util.List;
import java.util.Objects;

/**
 * Represents a value fragment of a Fauna query.
 * This class encapsulates a value that can be a variable in the query.
 */
public class QueryArr<E extends QueryFragment> extends QueryFragment<List<E>> {
    public static <E extends QueryFragment> QueryArr of(List<E> val) {
        return new QueryArr<>(val);
    }

    private final List<E> value;

    /**
     * Constructs a ValueFragment with the specified value.
     *
     * @param value the value to encapsulate.
     */
    public QueryArr(List<E> value) {
        this.value = value;
    }

    /**
     * Retrieves the encapsulated value of this fragment.
     *
     * @return the encapsulated object.
     */
    @Override
    public List<E> get() {
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

        QueryArr that = (QueryArr) o;

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
