package com.fauna.query.model;

import java.util.Objects;

/**
 * Class representing a reference to a named document in Fauna.
 * It extends BaseReference by adding a name for the specific document.
 */
public class NamedDocumentReference extends BaseReference {

    /**
     * Named document identifier.
     */
    private final String name;

    /**
     * Constructs a NamedDocumentReference with a collection name and document name.
     *
     * @param coll A string representing the collection name.
     * @param name The named document's identifier.
     */
    public NamedDocumentReference(String coll, String name) {
        super(coll);
        this.name = name;
    }

    /**
     * Constructs a NamedDocumentReference with a Module and document name.
     *
     * @param coll A Module object representing the collection.
     * @param name The named document's identifier.
     */
    public NamedDocumentReference(Module coll, String name) {
        super(coll);
        this.name = name;
    }

    /**
     * Gets the named document identifier.
     *
     * @return The string representing the named document's identifier.
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NamedDocumentReference that = (NamedDocumentReference) o;

        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
