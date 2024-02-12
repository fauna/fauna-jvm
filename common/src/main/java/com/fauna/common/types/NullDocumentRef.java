package com.fauna.common.types;

/**
 * Represents a null reference to a document, including a reason for its null state.
 */
public class NullDocumentRef extends DocumentRef {

    private String cause;

    /**
     * Constructs a new DocumentRef object with the specified id and collection.
     *
     * @param id    The string value of the document ref id.
     * @param coll  The module to which the document ref belongs.
     * @param cause The cause to the name of the document ref be null.
     */
    public NullDocumentRef(String id, Module coll, String cause) {
        super(id, coll);
        this.cause = cause;
    }

    /**
     * Gets the cause that the document is null.
     *
     * @return A string representing the cause that the document is null.
     */
    public String getCause() {
        return cause;
    }

    /**
     * Sets the cause that the document is null.
     *
     * @param cause A string representing the cause that the document is null.
     */
    public void setCause(String cause) {
        this.cause = cause;
    }
}
