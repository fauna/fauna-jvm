package com.fauna.types;

import java.util.Objects;

public final class NonNull<T> extends Nullable<T> {

    public NonNull(T val) {
        super(val);
    }

    @Override
    public T get() {
        return val;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null) return false;

        if (getClass() != o.getClass()) return false;

        if (val.getClass() != ((NonNull<?>) o).get().getClass()) return false;

        return val.equals(((NonNull<?>) o).get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(val);
    }
}
