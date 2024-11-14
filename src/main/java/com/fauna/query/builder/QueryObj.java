package com.fauna.query.builder;

import java.util.Map;
import java.util.Objects;

/**
 * This class represents a special type of query fragment that allows users
 * to provide Fauna with an object whose values are individual queries.
 * Each of these queries will be evaluated, and the result of each query
 * will be a value in the returned object.
 *
 * <p>
 * Example usage:
 * Given a map of queries:
 * <pre>
 *   var o = Map.of("key1", fql("1 + 1"));
 * </pre>
 * If this map is passed directly to a query, it will fail because the entire
 * object will be treated as a value. However, if you wrap the map in a
 * {@code QueryObj}, Fauna will evaluate each query.
 * <pre>
 *   client.query(fql("${obj}", Map.of("obj", o));
 *   // Error: the map is treated as a value.
 *
 *   client.query(fql("${obj}", Map.of("obj", QueryObj.of(o)))
 *   // Result: { "key1": 2 }
 * </pre>
 *
 * @param <E> The type of {@code QueryObj}. Must be a subtype of {@code QueryFragment}.
 */
@SuppressWarnings("rawtypes")
public final class QueryObj<E extends QueryFragment> extends QueryFragment<Map<String, E>> {

    /**
     * Creates a new {@code QueryObj} instance with the specified map of query fragments.
     *
     * @param val the map of query fragments to wrap.
     * @param <E> The map value type, which must extend {@code QueryFragment}.
     * @return a new {@code QueryObj} instance wrapping the provided map.
     */
    public static <E extends QueryFragment> QueryObj of(final Map<String, E> val) {
        //noinspection unchecked
        return new QueryObj(val);
    }

    private final Map<String, E> value;

    /**
     * Constructs a new {@code QueryObj} with the given map of query fragments.
     *
     * @param value the map to encapsulate.
     */
    public QueryObj(final Map<String, E> value) {
        this.value = value;
    }

    /**
     * Retrieves the encapsulated map of query fragments that make up this query object.
     *
     * @return the map of query fragments.
     */
    @Override
    public Map<String, E> get() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
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

    /**
     * Retrieves the wrapped map value.
     *
     * @return the encapsulated map.
     */
    public Map<String, E> getValue() {
        return this.value;
    }
}
