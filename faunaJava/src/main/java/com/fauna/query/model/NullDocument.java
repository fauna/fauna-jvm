package com.fauna.query.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a null or non-existent document within the Fauna database.
 * This class provides a way to encapsulate a reference to a document that
 * might not be present, along with an optional cause for its absence.
 */
public class NullDocument {

    /**
     * Reference to the potentially null document.
     */
    private final BaseReference ref;

    /**
     * Reason for the document's absence, if any
     */
    private final String cause;

    /**
     * Constructs a NullDocument with a given reference and an optional cause.
     *
     * @param ref   A reference to the document that is null. This can be an instance of
     *              DocumentReference or NamedDocumentReference.
     * @param cause An optional string describing the cause of the document's null status.
     */
    public NullDocument(BaseReference ref, String cause) {
        this.ref = ref;
        this.cause = cause;
    }

    /**
     * Retrieves the reference to the null document.
     *
     * @return The reference to the null document.
     */
    public BaseReference getRef() {
        return ref;
    }

    /**
     * Retrieves an Optional containing the cause for the document's absence.
     * The Optional is empty if there is no cause provided.
     *
     * @return An Optional containing the cause of absence, if available.
     */
    public Optional<String> getCause() {
        return Optional.ofNullable(cause);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NullDocument that = (NullDocument) o;

        if (!Objects.equals(ref, that.ref)) return false;
        return Objects.equals(cause, that.cause);
    }

    @Override
    public int hashCode() {
        int result = ref != null ? ref.hashCode() : 0;
        result = 31 * result + (cause != null ? cause.hashCode() : 0);
        return result;
    }
}
