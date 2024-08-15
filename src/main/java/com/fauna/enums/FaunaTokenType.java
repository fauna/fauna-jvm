package com.fauna.enums;

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

    MODULE,
    END_SET,
    START_SET;

    public FaunaTokenType getEndToken() throws IOException {
        switch (this) {
            case START_DOCUMENT: return END_DOCUMENT;
            case START_OBJECT: return END_OBJECT;
            case START_ARRAY: return END_ARRAY;
            case START_PAGE: return END_PAGE;
            case START_SET: return END_SET;
            case START_REF: return END_REF;
            default: throw new IOException("No end token for " + this.name());
        }
    }
}
