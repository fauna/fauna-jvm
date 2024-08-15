package com.fauna.types;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a document.
 */
public final class Document extends BaseDocument {

    private final String id;

    /**
     * Initializes a new instance of the Document class with the specified id, coll, and ts.
     *
     * @param id   The string value of the document id.
     * @param coll The module to which the document belongs.
     * @param ts   The timestamp of the document.
     */
    public Document(String id, Module coll, Instant ts) {
        super(coll, ts);
        this.id = id;
    }

    /**
     * Initializes a new instance of the Document class with the specified id, coll, ts, and
     * additional data stored as key/value pairs on the instance.
     *
     * @param id   The string value of the document id.
     * @param coll The module to which the document belongs.
     * @param ts   The timestamp of the document.
     * @param data Additional data on the document.
     */
    public Document(String id, Module coll, Instant ts, Map<String, Object> data) {
        super(coll, ts, data);
        this.id = id;
    }

    /**
     * Gets the string value of the document id.
     *
     * @return The string value of the document id.
     */
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null) return false;

        if (getClass() != o.getClass()) return false;

        Document c = (Document) o;

        return id.equals(c.id)
                && getTs().equals(c.getTs())
                && getCollection().equals(c.getCollection())
                && data.equals(c.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, getTs(), getCollection(), data);
    }
}
