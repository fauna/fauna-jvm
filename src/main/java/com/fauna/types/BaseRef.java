package com.fauna.types;

/**
 * Represents a reference to a document within a collection.
 * This abstract class serves as a base for specific types of document references,
 * encapsulating the collection to which the reference belongs.
 */
public abstract class BaseRef {

    private final Module collection;

    /**
     * Constructs a new {@code BaseRef} object with the specified collection.
     *
     * @param coll The module to which the document reference belongs.
     */
    public BaseRef(final Module coll) {
        this.collection = coll;
    }

    /**
     * Gets the collection to which this reference belongs.
     *
     * @return The {@code Module} representing the collection associated with this reference.
     */
    public Module getCollection() {
        return collection;
    }

    /**
     * Indicates whether some other object is "equal to" this reference.
     * This method should be overridden in subclasses to provide specific equality logic.
     *
     * @param o the reference object with which to compare.
     * @return {@code true} if this reference is the same as the object argument;
     *         {@code false} otherwise.
     */
    @Override
    public abstract boolean equals(Object o);

    /**
     * Returns a hash code value for the object.
     * This method should be overridden in subclasses to provide specific hash code logic.
     *
     * @return a hash code value for this reference.
     */
    @Override
    public abstract int hashCode();
}
