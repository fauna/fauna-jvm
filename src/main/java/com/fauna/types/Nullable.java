package com.fauna.types;

public abstract class Nullable<T> {
    final T val;

    public Nullable(T val) {
        this.val = val;
    }

    public abstract T get();
}
