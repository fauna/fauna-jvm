package com.fauna.types;

import java.util.Objects;

public final class NonNullDocument<T> extends NullableDocument<T> {

    public NonNullDocument(T val) {
        super(val);
    }

    /**
     * Get the wrapped value.
     *
     * @return The wrapped value
     */
    @Override
    public T get() {
        return val;
    }

    /**
     * Get the wrapped value.
     *
     * @return The wrapped value
     */
    public T getValue() {
        // Allows for default serialization without attributes.
        return get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null) return false;

        if (getClass() != o.getClass()) return false;

        if (val.getClass() != ((NonNullDocument<?>) o).get().getClass()) return false;

        return val.equals(((NonNullDocument<?>) o).get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(val);
    }
}
