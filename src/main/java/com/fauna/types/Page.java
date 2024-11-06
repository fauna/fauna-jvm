package com.fauna.types;

import com.fauna.query.AfterToken;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a page in a dataset for pagination.
 *
 * @param <T> The type of data contained in the page.
 */
public class Page<T> {
    private final List<T> data;
    private final String after;

    public Page(List<T> data, String after) {
        this.data = data;
        this.after = after;
    }

    public List<T> getData() {
        return data;
    }

    public Optional<AfterToken> getAfter() {
        return AfterToken.fromString(after);
    }

    @Override
    public boolean equals(Object o) {
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

    @Override
    public int hashCode() {
        return Objects.hash(after, data);
    }
}