package com.fauna.query.model;

import java.util.Objects;

/**
 * Class representing a reference to a Document in Fauna.
 * It extends BaseReference by adding an identifier for the specific document.
 */
public class DocumentReference extends BaseReference {

    /**
     * Document identifier
     */
    private final String id;

    /**
     * Constructs a DocumentReference with a collection name and document ID.
     *
     * @param coll A string representing the collection name.
     * @param id   The document's identifier.
     */
    public DocumentReference(String coll, String id) {
        super(coll);
        this.id = id;
    }

    /**
     * Constructs a DocumentReference with a Module and document ID.
     *
     * @param coll A Module object representing the collection.
     * @param id   The document's identifier.
     */
    public DocumentReference(Module coll, String id) {
        super(coll);
        this.id = id;
    }

    /**
     * Gets the document ID.
     *
     * @return The string representing the document's identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * Factory method to create a DocumentReference from a formatted string.
     *
     * @param ref A string of the format "CollectionName:ID".
     * @return A new DocumentReference parsed from the string.
     * @throws IllegalArgumentException If the format of the reference string is incorrect.
     */
    public static DocumentReference fromString(String ref) {
        String[] parts = ref.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Expects string of format <CollectionName>:<ID>");
        }
        return new DocumentReference(parts[0], parts[1]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DocumentReference that = (DocumentReference) o;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
