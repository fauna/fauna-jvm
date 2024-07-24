package com.fauna.types;

/**
 * Represents a reference to a named document that is null, including a reason for its null state.
 * This class extends NamedDocumentRef to provide additional context for null references in the
 * database.
 */
public class NullNamedDocumentRef extends NamedDocumentRef {

    private String cause;

    /**
     * Constructs a new NamedDocumentRef object with the specified name and collection.
     *
     * @param name  The string value of the named document ref name.
     * @param coll  The module to which the named document ref belongs.
     * @param cause The cause to the name of the named document be null.
     */
    public NullNamedDocumentRef(String name, Module coll, String cause) {
        super(name, coll);
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
}
