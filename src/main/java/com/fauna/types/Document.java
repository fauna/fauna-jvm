package com.fauna.types;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an immutable document with an ID, associated collection, timestamp, and optional key-value data.
 * This class extends {@link BaseDocument} to provide additional document-specific functionality,
 * such as unique identification and data equality checks.
 */
public final class Document extends BaseDocument {

    private final String id;

    /**
     * Initializes a new instance of the {@code Document} class with the specified ID, collection, and timestamp.
     *
     * @param id   The unique string identifier of the document.
     * @param coll The module (collection) to which the document belongs.
     * @param ts   The timestamp indicating the document's creation or last modification.
     */
    public Document(final String id, final Module coll, final Instant ts) {
        super(coll, ts);
        this.id = id;
    }

    /**
     * Initializes a new instance of the {@code Document} class with the specified ID, collection,
     * timestamp, and additional key-value data.
     *
     * @param id   The unique string identifier of the document.
     * @param coll The module (collection) to which the document belongs.
     * @param ts   The timestamp indicating the document's creation or last modification.
     * @param data Additional data for the document, represented as a map of key-value pairs.
     */
    public Document(
            final String id,
            final Module coll,
            final Instant ts,
            final Map<String, Object> data) {
        super(coll, ts, data);
        this.id = id;
    }

    /**
     * Gets the unique identifier for this document.
     *
     * @return A {@code String} representing the document's unique ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Checks if this document is equal to another object. Two documents are considered equal
     * if they have the same ID, timestamp, collection, and data content.
     *
     * @param o The object to compare with this document for equality.
     * @return {@code true} if the specified object is equal to this document; otherwise, {@code false}.
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

        Document c = (Document) o;

        return id.equals(c.id)
                && getTs().equals(c.getTs())
                && getCollection().equals(c.getCollection())
                && getData().equals(c.getData());
    }

    /**
     * Returns a hash code value for this document based on its ID, timestamp, collection, and data.
     *
     * @return An integer hash code for this document.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, getTs(), getCollection(), getData());
    }
}
