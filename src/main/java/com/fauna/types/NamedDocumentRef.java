package com.fauna.types;

import java.util.Objects;

/**
 * Represents a document ref that has a "name" instead of an "id". For example, a Role document
 * reference is represented as a NamedDocumentRef.
 */
public class NamedDocumentRef {

    private String name;
    private Module collection;

    /**
     * Constructs a new NamedDocumentRef object with the specified name and collection.
     *
     * @param name The string value of the named document ref name.
     * @param coll The module to which the named document ref belongs.
     */
    public NamedDocumentRef(String name, Module coll) {
        this.name = name;
        this.collection = coll;
    }

    /**
     * Gets the string value of the ref name.
     *
     * @return The string value of the ref name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the string value of the ref name.
     *
     * @param name The string value of the ref name.
     */
    public void setName(String name) {
        this.name = name;
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

        NamedDocumentRef c = (NamedDocumentRef) o;

        return name.equals(c.name)
                && getCollection().equals(c.getCollection());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, getCollection());
    }
}
