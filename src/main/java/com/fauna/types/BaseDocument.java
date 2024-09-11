package com.fauna.types;

import java.time.Instant;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * Represents the base structure of a document.
 */
public abstract class BaseDocument implements Iterable<BaseDocument.Entry>  {

    protected final Hashtable<String, Object> data = new Hashtable<>();
    private final Instant ts;
    private final Module collection;

    /**
     * Initializes a new instance of the {@code BaseDocument} class with the specified collection
     * and timestamp.
     *
     * @param coll The collection to which the document belongs.
     * @param ts   The timestamp of the document.
     */
    public BaseDocument(Module coll, Instant ts) {
        this.collection = coll;
        this.ts = ts;
    }

    /**
     * Initializes a new instance of the {@code BaseDocument} class with the specified collection,
     * timestamp, and initial data.
     *
     * @param coll The collection to which the document belongs.
     * @param ts   The timestamp of the document.
     * @param data Initial data for the document in key-value pairs.
     */
    public BaseDocument(Module coll, Instant ts, Map<String, Object> data) {
        this(coll, ts);
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            this.data.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Returns an iterator over the elements in this document.
     *
     * @return an iterator over the elements in this document.
     */
    @Override
    public Iterator<Entry> iterator() {
        return new Iterator<Entry>() {
            private final Enumeration<String> keys = data.keys();

            @Override
            public boolean hasNext() {
                return keys.hasMoreElements();
            }

            @Override
            public Entry next() {
                String key = keys.nextElement();
                return new Entry(key, data.get(key));
            }
        };
    }

    /**
     * Represents a key-value pair in the document.
     */
    public static class Entry {

        private final String key;
        private final Object value;

        public Entry(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }
    }

    /**
     * Gets the timestamp of the document.
     *
     * @return The Instant of the document.
     */
    public Instant getTs() {
        return ts;
    }

    /**
     * Gets the collection to which the document belongs.
     *
     * @return The collection to which the document belongs.
     */
    public Module getCollection() {
        return collection;
    }

    /**
     * Gets a copy of the underlying data as a Map.
     *
     * @return The data.
     */
    public Map<String,Object> getData() {
        return Map.copyOf(data);
    }

    /**
     * Gets the count of key-value pairs contained in the document.
     *
     * @return The number of key-value pairs.
     */
    public int size() {
        return data.size();
    }

    /**
     * Determines whether the document contains the specified key.
     *
     * @param key The key to locate in the document.
     * @return {@code true} if the document contains an element with the specified key; otherwise,
     * {@code false}.
     */
    public boolean containsKey(String key) {
        return data.containsKey(key);
    }

    /**
     * Gets the value associated with the specified key.
     *
     * @param key The key of the value to get.
     * @return The value associated with the specified key, or {@code null} if the key is not found.
     */
    public Object get(String key) {
        return data.get(key);
    }
}
