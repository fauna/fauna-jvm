package com.fauna.types;


import java.util.Objects;


public abstract class BaseRef {

    private final Module collection;

    /**
     * Constructs a new Ref object with the specified id and collection.
     *
     * @param coll The module to which the document ref belongs.
     */
    public BaseRef(Module coll) {
        this.collection = coll;
    }

    /**
     * Gets the collection to which the ref belongs.
     *
     * @return The collection to which the ref belongs.
     */
    public Module getCollection() {
        return collection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null) return false;

        if (getClass() != o.getClass()) return false;

        BaseRef c = (BaseRef) o;

        return getCollection().equals(c.getCollection());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCollection());
    }
}
