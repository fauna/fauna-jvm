package com.fauna.types;

import java.util.Objects;

/**
 * Represents a reference to a document identified by a "name" instead of an "id".
 * This class is used for references to system collection documents where a unique name
 * (e.g., for a Role) is used instead of a numeric or auto-generated ID.
 */
public final class NamedDocumentRef extends BaseRef {

    private final String name;

    /**
     * Constructs a new {@code NamedDocumentRef} object with the specified name and collection.
     *
     * @param name The unique string name identifying the document reference.
     * @param coll The module (collection) to which the named document reference belongs.
     */
    public NamedDocumentRef(
            final String name,
            final Module coll) {
        super(coll);
        this.name = name;
    }

    /**
     * Gets the unique name of the document reference.
     *
     * @return A {@code String} representing the name of the document reference.
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if this document reference is equal to another object. Two document references
     * are considered equal if they have the same name and belong to the same collection.
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

        NamedDocumentRef c = (NamedDocumentRef) o;

        return name.equals(c.name)
                && getCollection().equals(c.getCollection());
    }

    /**
     * Returns a hash code value for this document reference based on its name and collection.
     *
     * @return An integer hash code for this document reference.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, getCollection());
    }
}
