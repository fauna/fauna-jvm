package com.fauna.query.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a specific document in the Fauna database with a unique identifier and timestamp.
 * This class is immutable and extends BaseDocument.
 */
public class Document extends BaseDocument {

    private final String id;
    private final LocalDateTime ts;
    private final Module coll;

    /**
     * Constructs a new Document with the specified identifier, timestamp, collection reference, and data.
     *
     * @param id   The unique identifier for this document.
     * @param ts   The timestamp of the document.
     * @param coll The collection to which this document belongs.
     * @param data The data map containing the document's properties.
     */
    public Document(String id, LocalDateTime ts, Module coll, Map<String, Object> data) {
        super(data);
        this.id = Objects.requireNonNull(id, "Document id cannot be null");
        this.ts = Objects.requireNonNull(ts, "Document timestamp cannot be null");
        this.coll = Objects.requireNonNull(coll, "Document collection reference cannot be null");
    }

    /**
     * Retrieves the unique identifier for this document.
     *
     * @return The unique identifier for this document.
     */
    public String getId() {
        return id;
    }

    /**
     * Retrieves the timestamp of when this document was last updated or created.
     *
     * @return The timestamp of this document.
     */
    public LocalDateTime getTs() {
        return ts;
    }

    /**
     * Retrieves the collection reference in which this document is stored.
     *
     * @return The collection reference as a Module.
     */
    public Module getColl() {
        return coll;
    }

}
