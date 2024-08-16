package com.fauna.types;


import java.util.Objects;

/**
 * Represents a document ref.
 */
public class DocumentRef extends BaseRef {

    private final String id;

    /**
     * Constructs a new Ref object with the specified id and collection.
     *
     * @param id   The string value of the document ref id.
     * @param coll The module to which the document ref belongs.
     */
    public DocumentRef(String id, Module coll) {
        super(coll);
        this.id = id;
    }

    /**
     * Gets the string value of the ref id.
     *
     * @return The string value of the ref id.
     */
    public String getId() {
        return id;
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
