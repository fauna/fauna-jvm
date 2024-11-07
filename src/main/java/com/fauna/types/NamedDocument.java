package com.fauna.types;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a document identified by a "name" rather than an "id".
 * This class is commonly used for documents in system collections where a unique name
 * (e.g., for a Role) is more relevant than a numeric or auto-generated ID.
 */
public final class NamedDocument extends BaseDocument {

    /**
     * The unique name identifier for this document.
     */
    private final String name;

    /**
     * Initializes a new instance of the {@code NamedDocument} class with the specified
     * name, collection, and timestamp.
     *
     * @param name The unique string name of the document.
     * @param coll The module (collection) to which the document belongs.
     * @param ts   The timestamp indicating the document's creation or last modification.
     */
    public NamedDocument(
            final String name,
            final Module coll,
            final Instant ts) {
        super(coll, ts);
        this.name = name;
    }

    /**
     * Initializes a new instance of the {@code NamedDocument} class with the specified
     * name, collection, timestamp, and additional data.
     *
     * @param name The unique string name of the document.
     * @param coll The module (collection) to which the document belongs.
     * @param ts   The timestamp indicating the document's creation or last modification.
     * @param data Additional key-value data to store in the document.
     */
    public NamedDocument(
            final String name,
            final Module coll,
            final Instant ts,
            final Map<String, Object> data) {
        super(coll, ts, data);
        this.name = name;
    }

    /**
     * Gets the unique name of the document.
     *
     * @return A {@code String} representing the document's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if this document is equal to another object. Two documents are considered equal
     * if they have the same name, timestamp, collection, and data.
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

        NamedDocument c = (NamedDocument) o;

        return name.equals(c.name)
                && getTs().equals(c.getTs())
                && getCollection().equals(c.getCollection())
                && getData().equals(c.getData());
    }

    /**
     * Returns a hash code value for this document based on its name, timestamp, collection, and data.
     *
     * @return An integer hash code for this document.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, getTs(), getCollection(), getData());
    }
}
