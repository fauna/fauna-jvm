package com.fauna.query.model;

/**
 * Abstract base class for reference models in Fauna query representation.
 * It holds a reference to a Module object representing a collection in the database.
 */
public abstract class BaseReference {

    /**
     * Module object representing a collection
     */
    protected Module coll;

    /**
     * Constructs a BaseReference with a collection name which is converted to a Module.
     *
     * @param coll A string representation of the collection name.
     */
    public BaseReference(String coll) {
        this.coll = new Module(coll);
    }

    /**
     * Constructs a BaseReference with a given Module object.
     *
     * @param coll A Module object representing the collection.
     */
    public BaseReference(Module coll) {
        this.coll = coll;
    }

    /**
     * Gets the collection Module object.
     *
     * @return The Module object representing the collection.
     */
    public Module getColl() {
        return coll;
    }

}
