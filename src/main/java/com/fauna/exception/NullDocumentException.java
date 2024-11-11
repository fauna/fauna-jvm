package com.fauna.exception;

import com.fauna.types.Module;

/**
 * Exception representing a <a href="https://docs.fauna.com/fauna/current/reference/fql/types/#nulldoc"≥null document</a> error in Fauna.
 * <p>
 * This exception is thrown when a document is null in Fauna, providing details about
 * the document ID, its collection, and the reason it is null.
 * Extends {@link FaunaException} to provide information specific to null document scenarios.
 */
public class NullDocumentException extends FaunaException {

    private final String id;
    private final Module coll;
    private final String nullCause;

    /**
     * Constructs a new {@code NullDocumentException} with the specified document ID, collection, and cause.
     *
     * @param id        The ID of the null document.
     * @param coll      The {@link Module} representing the collection of the document.
     * @param nullCause A {@code String} describing the reason the document is null.
     */
    public NullDocumentException(final String id, final Module coll, final String nullCause) {
        super(String.format("Document %s in collection %s is null: %s", id,
                coll != null ? coll.getName() : "unknown", nullCause));
        this.id = id;
        this.coll = coll;
        this.nullCause = nullCause;
    }

    /**
     * Retrieves the ID of the null document.
     *
     * @return A {@code String} representing the document ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Retrieves the collection associated with the null document.
     *
     * @return A {@link Module} representing the document's collection, or {@code null} if unknown.
     */
    public Module getCollection() {
        return coll;
    }

    /**
     * Retrieves the cause for the document being null.
     *
     * @return A {@code String} describing why the document is null.
     */
    public String getNullCause() {
        return nullCause;
    }
}
