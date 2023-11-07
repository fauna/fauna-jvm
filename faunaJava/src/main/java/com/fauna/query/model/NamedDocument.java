package com.fauna.query.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a named document in the Fauna database, such as definitions of collections, indexes, and roles.
 * This class is immutable and extends BaseDocument.
 */
public class NamedDocument extends BaseDocument {

    private final String name;
    private final LocalDateTime ts;
    private final Module coll;

    /**
     * Constructs a new NamedDocument with the specified name, timestamp, collection reference, and data.
     * @param name The name of this document.
     * @param ts The timestamp of the document.
     * @param coll The collection to which this named document belongs.
     * @param data The data map containing the document's properties.
     */
    public NamedDocument(String name, LocalDateTime ts, Module coll, Map<String, Object> data) {
        super(data);
        this.name = Objects.requireNonNull(name, "NamedDocument name cannot be null");
        this.ts = Objects.requireNonNull(ts, "NamedDocument timestamp cannot be null");
        this.coll = Objects.requireNonNull(coll, "NamedDocument collection reference cannot be null");
    }

    /**
     * Retrieves the name of this named document, which could represent a collection, index, role, etc.
     * @return The name of the named document.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the timestamp of when this named document was last updated or created.
     * @return The timestamp of the named document.
     */
    public LocalDateTime getTs() {
        return ts;
    }

    /**
     * Retrieves the collection reference in which this named document is stored.
     * @return The collection reference as a Module.
     */
    public Module getColl() {
        return coll;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        NamedDocument entries = (NamedDocument) o;

        if (!Objects.equals(name, entries.name)) return false;
        if (!Objects.equals(ts, entries.ts)) return false;
        return Objects.equals(coll, entries.coll);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (ts != null ? ts.hashCode() : 0);
        result = 31 * result + (coll != null ? coll.hashCode() : 0);
        return result;
    }
}
