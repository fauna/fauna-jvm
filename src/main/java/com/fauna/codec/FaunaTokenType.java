package com.fauna.codec;

import com.fauna.exception.ClientResponseException;

import java.io.IOException;

/**
 * Enumeration representing token types for Fauna serialization.
 */
public enum FaunaTokenType {
    NONE,

    START_OBJECT,
    END_OBJECT,

    START_ARRAY,
    END_ARRAY,

    START_PAGE,
    END_PAGE,

    START_REF,
    END_REF,

    START_DOCUMENT,
    END_DOCUMENT,

    FIELD_NAME,

    STRING,
    BYTES,

    INT,
    LONG,
    DOUBLE,

    DATE,
    TIME,

    TRUE,
    FALSE,

    NULL,

    STREAM,

    MODULE;

    public FaunaTokenType getEndToken() {
        switch (this) {
            case START_DOCUMENT: return END_DOCUMENT;
            case START_OBJECT: return END_OBJECT;
            case START_ARRAY: return END_ARRAY;
            case START_PAGE: return END_PAGE;
            case START_REF: return END_REF;
            default: throw new ClientResponseException("No end token for " + this.name());
        }
    }

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
