package com.fauna.query.builder;

import java.util.List;
import java.util.Objects;

/**
 * Represents a special type that allows Fauna to evaluate an array of individual
 * queries, each of which will be processed, and its result will be an element
 * in the returned array.
 * <p>
 * Example usage:
 * <pre>
 *   var listOfQueries = List.of(fql("1 + 1"), fql("2 + 2"), fql("3 + 3"));
 * </pre>
 * Directly providing this list to a query will fail because it would be treated
 * as a {@code QueryVal}. By wrapping it in a {@code QueryArr}, each query within the
 * list will be evaluated separately:
 * <pre>
 *   client.query(fql("${queries}", Map.of("queries", listOfQueries)));
 *   // Error: the list is treated as a single value.
 *
 *   client.query(fql("${queries}", Map.of("queries", QueryArr.of(listOfQueries)));
 *   // Returns: [2, 4, 6]
 * </pre>
 *
 * @param <E> The type of elements in the QueryArr, which must be a subtype of
 *            {@link QueryFragment}.
 */
@SuppressWarnings("rawtypes")
public final class QueryArr<E extends QueryFragment>
        extends QueryFragment<List<E>> {

    private final List<E> value;

    /**
     * Static factory method to create a new {@code QueryArr} instance.
     *
     * @param val the list of {@link QueryFragment} elements to wrap.
     * @param <E> the type of elements in the list, which must extend {@link QueryFragment}.
     * @return a new instance of {@code QueryArr} encapsulating the provided list.
     */
    public static <E extends QueryFragment> QueryArr<E> of(final List<E> val) {
        return new QueryArr<>(val);
    }

    /**
     * Constructs a {@code QueryArr} with the specified list of query fragments.
     *
     * @param value the list of query fragments to encapsulate.
     */
    public QueryArr(final List<E> value) {
        this.value = value;
    }

    /**
     * Retrieves the encapsulated list of query fragments in this {@code QueryArr}.
     *
     * @return the list of query fragments.
     */
    @Override
    public List<E> get() {
        return value;
    }

    /**
     * Checks if this {@code QueryArr} is equal to another object.
     * Two {@code QueryArr} objects are equal if they contain the same list of query fragments.
     *
     * @param o the object to compare with.
     * @return {@code true} if the specified object is equal to this {@code QueryArr};
     *         {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        QueryArr<?> that = (QueryArr<?>) o;

        return Objects.equals(value, that.value);
    }

    /**
     * Returns the hash code of this {@code QueryArr}, based on its encapsulated list of query fragments.
     *
     * @return the hash code of this object.
     */
    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    /**
     * Retrieves the encapsulated list directly.
     *
     * @return the encapsulated list of query fragments.
     */
    public List<E> getValue() {
        return this.value;
    }
}
