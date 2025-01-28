package com.fauna.codec;

/**
 * Enum representing various FQL data types used by Fauna for data storage and retrieval.
 * These types provide structured representations for
 * encoding and decoding data in FQL queries and responses.
 */
public enum FaunaType {

    /**
     * Represents an integer value in FQL.
     */
    Int,

    /**
     * Represents a long integer value in FQL.
     */
    Long,

    /**
     * Represents a double-precision floating-point number in FQL.
     */
    Double,

    /**
     * Represents a UTF-8 encoded string in FQL.
     */
    String,

    /**
     * Represents a date without time in FQL.
     * Dates are in ISO 8601 format (YYYY-MM-DD).
     */
    Date,

    /**
     * Represents an exact timestamp or time value in FQL.
     * Timestamps are in ISO 8601 format.
     */
    Time,

    /**
     * Represents a boolean value in FQL.
     */
    Boolean,

    /**
     * Represents an object in FQL.
     */
    Object,

    /**
     * Represents a reference to a document.
     */
    Ref,

    /**
     * Represents a complete document in FQL.
     */
    Document,

    /**
     * Represents an array (or list) of values in FQL.
     * Arrays are ordered collections of elements and can contain multiple data types.
     */
    Array,

    /**
     * Represents binary data encoded in Base64 within FQL.
     * Used for storing raw bytes of data.
     */
    Bytes,

    /**
     * Represents a null value in FQL, denoting the absence of a value.
     */
    Null,

    /**
     * Represents an event source in FQL.
     * Event sources are used to track events as event feeds or event streams.
     */
    Stream,

    /**
     * Represents a module in FQL, which serves as a symbolic object
     * with associated methods
     */
    Module,

    /**
     * Represents a pageable Set in FQL.
     */
    Set
}
