package com.fauna.common.enums;

/**
 * Enumeration representing token types for Fauna serialization.
 */
public enum FaunaTokenType {
    NONE,

    START_OBJECT,
    START_ESCAPED_OBJECT,
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
    START_SET
}
