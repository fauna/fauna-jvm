package com.fauna.decode;

public enum FaunaToken {
    VALUE_INT, // Corresponds to @int
    VALUE_LONG, // Corresponds to @long
    VALUE_DOUBLE, // Corresponds to @double
    VALUE_TRUE, // For true boolean values
    VALUE_FALSE, // For false boolean values
    VALUE_STRING,
    VALUE_BYTES, // Corresponds to @bytes
    VALUE_TIME, // Corresponds to @time
    VALUE_DATE, // Corresponds to @date
    VALUE_UUID, // Corresponds to @uuid
    VALUE_NULL,
    VALUE_MODULE, // Corresponds to @mod

    // Types with object payloads having START and END variants
    START_OBJECT, // Corresponds to @object
    END_OBJECT,
    START_ARRAY,
    END_ARRAY,
    START_REF, // Corresponds to @ref
    END_REF,
    START_DOC, // Corresponds to @doc
    END_DOC,
    START_SET, // Corresponds to @set
    END_SET,

    // Other tokens
    FIELD_NAME

}
