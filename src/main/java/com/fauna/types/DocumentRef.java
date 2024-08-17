package com.fauna.types;


import java.util.Objects;

/**
 * Represents a document ref.
 */
public class DocumentRef {

    private String id;
    private Module collection;

    /**
     * Constructs a new DocumentRef object with the specified id and collection.
     *
     * @param id   The string value of the document ref id.
     * @param coll The module to which the document ref belongs.
     */
    public DocumentRef(String id, Module coll) {
        this.id = id;
        this.collection = coll;
    }

    /**
     * Gets the string value of the ref id.
     *
     * @return The string value of the ref id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the string value of the ref id.
     *
     * @param id The string value of the ref id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the collection to which the ref belongs.
     *
     * @return The collection to which the ref belongs.
     */
    public Module getCollection() {
        return collection;
    }

    /**
     * Sets the collection to which the ref belongs.
     *
     * @param collection The collection to which the ref belongs.
     */
    public void setCollection(Module collection) {
        this.collection = collection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null) return false;

        if (getClass() != o.getClass()) return false;

        DocumentRef c = (DocumentRef) o;

        return id.equals(c.id)
                && getCollection().equals(c.getCollection());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, getCollection());
    }
}
