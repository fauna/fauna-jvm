package com.fauna.types;

public abstract class NullableDocument<T> {
    final T val;

    public NullableDocument(T val) {
        this.val = val;
    }

    public abstract T get();
}
