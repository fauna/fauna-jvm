package com.fauna.types;

import com.fauna.exception.NullDocumentException;

import java.util.Objects;

public final class NullDoc<T> extends Nullable<T> {

    private final String id;
    private final Module coll;
    private final String cause;

    public NullDoc(String id, Module coll, String cause) {
        super(null);
        this.id = id;
        this.coll = coll;
        this.cause = cause;
    }

    public String getCause() {
        return cause;
    }

    @Override
    public T get() {
        throw new NullDocumentException(id, coll, cause);
    }

    public String getId() {
        return id;
    }

    public Module getColl() {
        return coll;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null) return false;

        if (getClass() != o.getClass()) return false;

        var c = (NullDoc<?>) o;
        return id.equals(c.getId())
                && coll.equals(c.getColl())
                && cause.equals(c.getCause());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, coll, cause);
    }
}
