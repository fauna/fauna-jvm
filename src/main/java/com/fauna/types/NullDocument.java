package com.fauna.types;

import com.fauna.exception.NullDocumentException;

import java.util.Objects;

public final class NullDocument<T> extends NullableDocument<T> {

    private final String id;
    private final Module coll;
    private final String cause;

    public NullDocument(String id, Module coll, String cause) {
        super(null);
        this.id = id;
        this.coll = coll;
        this.cause = cause;
    }

    /**
     * Get the cause of the null document.
     *
     * @return A string describing the cause of the null document.
     */
    public String getCause() {
        return cause;
    }

    @Override
    public T get() {
        throw new NullDocumentException(id, coll, cause);
    }

    /**
     * Get the ID of the null document.
     *
     * @return The document ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Get the Collection associated with the null document.
     *
     * @return A Module representing the collection.
     */
    public Module getCollection() {
        return coll;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (getClass() != o.getClass()) {
            return false;
        }

        var c = (NullDocument<?>) o;
        return id.equals(c.getId())
                && coll.equals(c.getCollection())
                && cause.equals(c.getCause());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, coll, cause);
    }
}
