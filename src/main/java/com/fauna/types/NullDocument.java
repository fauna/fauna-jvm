package com.fauna.types;

import com.fauna.exception.NullDocumentException;

import java.util.Objects;

/**
 * Represents a document that is explicitly null, providing information about the cause of its null state.
 * This class extends {@link NullableDocument} and throws a {@link NullDocumentException} when accessed.
 *
 * @param <T> The type of the document content, although it will not contain an actual value.
 */
public final class NullDocument<T> extends NullableDocument<T> {

    private final String id;
    private final Module coll;
    private final String cause;

    /**
     * Constructs a {@code NullDocument} with the specified ID, collection, and cause of nullity.
     *
     * @param id    The unique identifier of the document.
     * @param coll  The module (collection) to which this null document belongs.
     * @param cause A description of the reason why the document is null.
     */
    public NullDocument(final String id, final Module coll, final String cause) {
        super();
        this.id = id;
        this.coll = coll;
        this.cause = cause;
    }

    /**
     * Retrieves the cause of the document's null state.
     *
     * @return A {@code String} describing the cause of the null document.
     */
    public String getCause() {
        return cause;
    }

    /**
     * Throws a {@link NullDocumentException} when called, as this document is explicitly null.
     *
     * @return Never returns a value, as it always throws an exception.
     * @throws NullDocumentException Always thrown to indicate that this is a null document.
     */
    @Override
    public T get() {
        throw new NullDocumentException(id, coll, cause);
    }

    /**
     * Retrieves the ID of the null document.
     *
     * @return A {@code String} representing the document's ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Retrieves the collection associated with the null document.
     *
     * @return A {@code Module} representing the collection to which this null document belongs.
     */
    public Module getCollection() {
        return coll;
    }

    /**
     * Checks if this null document is equal to another object.
     * Two {@code NullDocument} objects are considered equal if they have the same ID, collection, and cause.
     *
     * @param o The object to compare with this null document for equality.
     * @return {@code true} if the specified object is equal to this null document; otherwise, {@code false}.
     */
    @Override
    public boolean equals(final Object o) {
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

    /**
     * Returns a hash code value for this null document based on its ID, collection, and cause.
     *
     * @return An integer hash code for this null document.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, coll, cause);
    }
}
