package com.fauna.types;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a document that has a "name" instead of an "id". For example, a Role document is
 * represented as a NamedDocument.
 */
public final class NamedDocument extends BaseDocument {

    /**
     * The string value of the document name.
     */
    private final String name;

    /**
     * Initializes a new instance of the NamedDocument class with the specified name, coll, and ts.
     *
     * @param name The string value of the document name.
     * @param coll The module to which the document belongs.
     * @param ts   The timestamp of the document.
     */
    public NamedDocument(String name, Module coll, Instant ts) {
        super(coll, ts);
        this.name = name;
    }

    /**
     * Initializes a new instance of the NamedDocument class with the specified name, coll, ts, and
     * additional data stored as key/value pairs on the instance.
     *
     * @param name The string value of the document name.
     * @param coll The module to which the document belongs.
     * @param ts   The timestamp of the document.
     * @param data Additional data on the document.
     */
    public NamedDocument(String name, Module coll, Instant ts,
                         Map<String, Object> data) {
        super(coll, ts, data);
        this.name = name;
    }

    /**
     * Gets the string value of the document name.
     *
     * @return The name of the document.
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
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
                && data.equals(c.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, getTs(), getCollection(), data);
    }
}