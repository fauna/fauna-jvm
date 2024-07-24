package com.fauna.types;

import java.time.Instant;
import java.util.Map;

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
}