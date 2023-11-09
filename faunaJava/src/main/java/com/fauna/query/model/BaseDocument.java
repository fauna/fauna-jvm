package com.fauna.query.model;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Base class for documents, providing a read-only view of data as a map.
 * This class implements the Iterable interface, allowing iteration over the document's entries.
 */
public class BaseDocument implements Iterable<Map.Entry<String, Object>> {

    private final Map<String, Object> store;

    /**
     * Constructs a BaseDocument with the given map data.
     *
     * @param data A map containing the initial data for this document.
     */
    protected BaseDocument(Map<String, Object> data) {
        // Creates an immutable copy of the data map
        this.store = Map.copyOf(data);
    }

    /**
     * Retrieves the value to which the specified key is mapped.
     *
     * @param key The key whose associated value is to be returned.
     * @return The value to which the specified key is mapped, or null if this map contains no mapping for the key.
     */
    public Object get(String key) {
        return store.get(key);
    }

    /**
     * Returns the number of key-value mappings in this document.
     *
     * @return The number of key-value mappings in this document.
     */
    public int size() {
        return store.size();
    }

    @Override
    public Iterator<Map.Entry<String, Object>> iterator() {
        return store.entrySet().iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseDocument entries = (BaseDocument) o;

        return Objects.equals(store, entries.store);
    }

    @Override
    public int hashCode() {
        return store != null ? store.hashCode() : 0;
    }
}
