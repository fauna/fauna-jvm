package com.fauna.types;

import java.util.Objects;

/**
 * Represents a document that is guaranteed to have a non-null value.
 * This class extends {@link NullableDocument} and enforces non-null data
 * for the document by disallowing null values in its constructor.
 *
 * @param <T> The type of the document content.
 */
public final class NonNullDocument<T> extends NullableDocument<T> {

    /**
     * Constructs a {@code NonNullDocument} with the specified non-null value.
     *
     * @param val The document's content of type {@code T}. Must not be null.
     * @throws NullPointerException if {@code val} is null.
     */
    public NonNullDocument(final T val) {
        super(val);
    }

    /**
     * Retrieves the non-null wrapped value of the document.
     *
     * @return The non-null wrapped value of type {@code T}.
     */
    @Override
    public T get() {
        return getUnderlyingValue();
    }

    /**
     * Retrieves the non-null wrapped value of the document.
     * This method provides compatibility for default serialization.
     *
     * @return The non-null wrapped value of type {@code T}.
     */
    public T getValue() {
        return get();
    }

    /**
     * Checks if this document is equal to another object.
     * Two {@code NonNullDocument} objects are considered equal if they hold values of the same type and content.
     *
     * @param o The object to compare with this document for equality.
     * @return {@code true} if the specified object is equal to this document; otherwise, {@code false}.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (getClass() != o.getClass()) {
            return false;
        }

        if (get().getClass() != ((NonNullDocument<?>) o).get().getClass()) {
            return false;
        }

        return get().equals(((NonNullDocument<?>) o).get());
    }

    /**
     * Returns a hash code value for this document based on its non-null value.
     *
     * @return An integer hash code for this document.
     */
    @Override
    public int hashCode() {
        return Objects.hash(get());
    }
}
