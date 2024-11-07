package com.fauna.types;

import java.time.Instant;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * Represents the base structure of a document with key-value pairs, a timestamp, and an associated collection.
 * This class provides functionality to store, retrieve, and iterate over document data.
 */
public abstract class BaseDocument implements Iterable<BaseDocument.Entry> {

    private final Hashtable<String, Object> data = new Hashtable<>();
    private final Instant ts;
    private final Module collection;

    /**
     * Initializes a new instance of the {@code BaseDocument} class with the specified collection
     * and timestamp.
     *
     * @param coll The collection to which the document belongs.
     * @param ts   The timestamp indicating when the document was created or last modified.
     */
    public BaseDocument(final Module coll, final Instant ts) {
        this.collection = coll;
        this.ts = ts;
    }

    /**
     * Initializes a new instance of the {@code BaseDocument} class with the specified collection,
     * timestamp, and initial data.
     *
     * @param coll The collection to which the document belongs.
     * @param ts   The timestamp of the document.
     * @param data Initial data for the document represented as key-value pairs.
     */
    public BaseDocument(
            final Module coll,
            final Instant ts,
            final Map<String, Object> data) {
        this(coll, ts);
        this.data.putAll(data);
    }

    /**
     * Returns an iterator over the entries in this document.
     * Each entry represents a key-value pair in the document's data.
     *
     * @return an {@code Iterator<Entry>} over the elements in this document.
     */
    @Override
    public Iterator<Entry> iterator() {
        return new Iterator<>() {
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

        /**
         * Initializes an entry with a specified key and value.
         *
         * @param key   The key for the entry.
         * @param value The value associated with the key.
         */
        public Entry(final String key, final Object value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Gets the key of this entry.
         *
         * @return The key as a {@code String}.
         */
        public String getKey() {
            return key;
        }

        /**
         * Gets the value associated with this entry's key.
         *
         * @return The value as an {@code Object}.
         */
        public Object getValue() {
            return value;
        }
    }

    /**
     * Gets the timestamp of the document, indicating its creation or last modification time.
     *
     * @return An {@code Instant} representing the document's timestamp.
     */
    public Instant getTs() {
        return ts;
    }

    /**
     * Gets the collection to which this document belongs.
     *
     * @return The {@code Module} representing the document's collection.
     */
    public Module getCollection() {
        return collection;
    }

    /**
     * Retrieves a copy of the document's data as a {@code Map}.
     *
     * @return A {@code Map<String, Object>} containing the document's key-value pairs.
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Returns the number of key-value pairs contained in the document.
     *
     * @return The total number of key-value pairs in the document.
     */
    public int size() {
        return data.size();
    }

    /**
     * Determines whether the document contains the specified key.
     *
     * @param key The key to search for in the document.
     * @return {@code true} if the document contains an element with the specified key;
     *         otherwise, {@code false}.
     */
    public boolean containsKey(final String key) {
        return data.containsKey(key);
    }

    /**
     * Retrieves the value associated with the specified key.
     *
     * @param key The key of the value to retrieve.
     * @return The value associated with the specified key, or {@code null} if the key is not present.
     */
    public Object get(final String key) {
        return data.get(key);
    }
}
