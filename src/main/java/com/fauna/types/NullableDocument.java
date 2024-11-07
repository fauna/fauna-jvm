package com.fauna.types;

/**
 * Represents a generic document wrapper that may hold a value representing a document.
 * This abstract class provides a base for documents that can optionally hold a value.
 *
 * @param <T> The type of the document's content.
 */
public abstract class NullableDocument<T> {
    private final T value;

    /**
     * Constructs a {@code NullableDocument} without a value, initializing it to {@code null}.
     */
    public NullableDocument() {
        this.value = null;
    }

    /**
     * Constructs a {@code NullableDocument} with the specified value.
     *
     * @param val The value to wrap, which may be null.
     */
    public NullableDocument(final T val) {
        this.value = val;
    }

    /**
     * Retrieves the document's value. This method must be implemented by subclasses
     * to specify how the value should be accessed.
     *
     * @return The document's content of type {@code T}.
     */
    public abstract T get();

    /**
     * Provides protected access to the underlying value, allowing subclasses
     * to directly access the stored value without additional logic.
     *
     * @return The underlying value of type {@code T}, which may be {@code null}.
     */
    protected T getUnderlyingValue() {
        return value;
    }
}
