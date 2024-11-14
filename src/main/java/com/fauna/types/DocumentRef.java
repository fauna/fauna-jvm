package com.fauna.types;

import java.util.Objects;

/**
 * Represents a reference to a specific document within a collection.
 * This class provides a unique identifier for the document and references
 * the collection to which it belongs.
 */
public final class DocumentRef extends BaseRef {

    private final String id;

    /**
     * Constructs a new {@code DocumentRef} object with the specified ID and collection.
     *
     * @param id   The unique string identifier of the document reference.
     * @param coll The module (collection) to which the document reference belongs.
     */
    public DocumentRef(final String id, final Module coll) {
        super(coll);
        this.id = id;
    }

    /**
     * Gets the unique identifier of the document reference.
     *
     * @return A {@code String} representing the document reference ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Checks if this document reference is equal to another object. Two document references
     * are considered equal if they have the same ID and belong to the same collection.
     *
     * @param o The object to compare with this document reference for equality.
     * @return {@code true} if the specified object is equal to this document reference; otherwise, {@code false}.
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

        DocumentRef c = (DocumentRef) o;

        return id.equals(c.id)
                && getCollection().equals(c.getCollection());
    }

    /**
     * Returns a hash code value for this document reference based on its ID and collection.
     *
     * @return An integer hash code for this document reference.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, getCollection());
    }
}
