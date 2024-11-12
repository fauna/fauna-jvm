package com.fauna.codec;

import com.fauna.exception.ClientResponseException;

/**
 * Enumeration representing token types for Fauna serialization.
 * <p>
 * The {@code FaunaTokenType} enum defines various tokens that are used to
 * identify different elements and data structures within Fauna's serialization
 * and deserialization processes.
 * </p>
 */
public enum FaunaTokenType {
    NONE,

    /**
     * A structural token that starts an object.
     * Wire representation: <code>{</code>
     */
    START_OBJECT,

    /**
     * A structural token that ends an object.
     * Wire representation: <code>}</code>
     */
    END_OBJECT,

    /**
     * A structural token that starts an array.
     * Wire representation: {@code [}
     */
    START_ARRAY,

    /**
     * A structural token that ends an array.
     * Wire representation: {@code ]}
     */
    END_ARRAY,

    /**
     * A structural token that starts a page.
     * Wire representation: <code>{ "@page":</code>
     */
    START_PAGE,

    /**
     * A structural token that ends a page.
     * Wire representation: <code>}</code>
     */
    END_PAGE,

    /**
     * A structural token that starts a ref.
     * Wire representation: <code>{ "@ref":</code>
     */
    START_REF,

    /**
     * A structural token that ends a ref.
     * Wire representation: <code>}</code>
     */
    END_REF,

    /**
     * A structural token that starts a document.
     * Wire representation: <code>{ "@doc":</code>
     */
    START_DOCUMENT,

    /**
     * A structural token that ends a document.
     * Wire representation: <code>}</code>
     */
    END_DOCUMENT,

    /**
     * A value token that represents a field of an Fauna object, document, or other structure.
     */
    FIELD_NAME,

    /**
     * A value token that represents a Fauna string.
     */
    STRING,

    /**
     * A value token that represents a Fauna base64-encoded byte sequence.
     */
    BYTES,

    /**
     * A value token that represents a Fauna integer.
     */
    INT,

    /**
     * A value token that represents a Fauna long.
     */
    LONG,

    /**
     * A value token that represents a Fauna double.
     */
    DOUBLE,

    /**
     * A value token that represents a Fauna date.
     */
    DATE,

    /**
     * A value token that represents a Fauna time.
     */
    TIME,

    /**
     * A value token that represents the Fauna boolean {@code true}.
     */
    TRUE,

    /**
     * A value token that represents the Fauna boolean {@code false}.
     */
    FALSE,

    /**
     * A value token that represents null.
     */
    NULL,

    /**
     * A value token that represents a Fauna Event Source.
     */
    STREAM,

    /**
     * A value token that represents a Fauna symbolic object, such as a user collection.
     */
    MODULE;

    /**
     * Returns the corresponding end token for the current start token.
     * <p>
     * For tokens representing the beginning of a structure (e.g., {@code START_OBJECT}),
     * this method returns the matching token for the end of that structure
     * (e.g., {@code END_OBJECT}).
     * </p>
     *
     * @return The end token associated with the current start token.
     * @throws ClientResponseException If the current token has no corresponding end token.
     */
    public FaunaTokenType getEndToken() {
        switch (this) {
            case START_DOCUMENT:
                return END_DOCUMENT;
            case START_OBJECT:
                return END_OBJECT;
            case START_ARRAY:
                return END_ARRAY;
            case START_PAGE:
                return END_PAGE;
            case START_REF:
                return END_REF;
            default:
                throw new ClientResponseException("No end token for " + this.name());
        }
    }

    /**
     * Returns the {@link FaunaType} that corresponds to the current {@code FaunaTokenType}.
     * <p>
     * This method maps each token type in {@code FaunaTokenType} to a specific {@code FaunaType},
     * which represents the underlying data type in Fauna's type system.
     * </p>
     *
     * @return The {@link FaunaType} associated with the current token type.
     * @throws IllegalStateException If the token type does not have an associated {@code FaunaType}.
     */
    public FaunaType getFaunaType() {
        switch (this) {
            case START_OBJECT:
            case END_OBJECT:
                return FaunaType.Object;
            case START_ARRAY:
            case END_ARRAY:
                return FaunaType.Array;
            case START_PAGE:
            case END_PAGE:
                return FaunaType.Set;
            case START_REF:
            case END_REF:
                return FaunaType.Ref;
            case START_DOCUMENT:
            case END_DOCUMENT:
                return FaunaType.Document;
            case STRING:
                return FaunaType.String;
            case BYTES:
                return FaunaType.Bytes;
            case INT:
                return FaunaType.Int;
            case LONG:
                return FaunaType.Long;
            case DOUBLE:
                return FaunaType.Double;
            case DATE:
                return FaunaType.Date;
            case TIME:
                return FaunaType.Time;
            case TRUE:
            case FALSE:
                return FaunaType.Boolean;
            case NULL:
                return FaunaType.Null;
            case STREAM:
                return FaunaType.Stream;
            case MODULE:
                return FaunaType.Module;
            default:
                throw new IllegalStateException("No associated FaunaType for FaunaTokenType: " + this);
        }
    }
}
