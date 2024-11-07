package com.fauna.types;

import com.fauna.query.AfterToken;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a page of data in a Fauna Set. Supports <a
 * href="https://docs.fauna.com/fauna/current/learn/query/pagination/">pagination</a> with an optional `after`
 * token for retrieving additional pages.
 *
 * @param <T> The type of data contained within the page.
 */
public final class Page<T> {
    private final List<T> data;
    private final String after;

    /**
     * Constructs a {@code Page} with the specified data and an optional after token.
     *
     * @param data  The list of data items belonging to this page.
     * @param after The after token for pagination, which may be null if there are no more pages.
     */
    public Page(final List<T> data, final String after) {
        this.data = data;
        this.after = after;
    }

    /**
     * Retrieves the data items contained in this page.
     *
     * @return A {@code List<T>} of data items belonging to this page.
     */
    public List<T> getData() {
        return data;
    }

    /**
     * Retrieves the optional after token for pagination. If present, this token can be used to
     * request the next page of results from Fauna.
     *
     * @return An {@code Optional<AfterToken>} representing the after token, or an empty {@code Optional} if no token
     * is present.
     */
    public Optional<AfterToken> getAfter() {
        return AfterToken.fromString(after);
    }

    /**
     * Checks if this page is equal to another object. Two pages are considered equal
     * if they have the same data and after token.
     *
     * @param o The object to compare with this page for equality.
     * @return {@code true} if the specified object is equal to this page; otherwise, {@code false}.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o instanceof Page) {
            @SuppressWarnings("rawtypes")
            Page c = (Page) o;
            return Objects.equals(after, c.after)
                    && data.equals(c.data);
        } else {
            return false;
        }
    }

    /**
     * Returns a hash code value for this page based on its data and after token.
     *
     * @return An integer hash code for this page.
     */
    @Override
    public int hashCode() {
        return Objects.hash(after, data);
    }
}
