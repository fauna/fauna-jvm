package com.fauna.codec;

/**
 * Enum representing various data types used by Fauna for data storage and retrieval.
 * These types align with Fauna's type system, providing structured representations for
 * encoding and decoding data in Fauna queries and responses.
 */
public enum FaunaType {

    /**
     * Represents an integer value in Fauna.
     */
    Int,

    /**
     * Represents a long integer value in Fauna.
     */
    Long,

    /**
     * Represents a double-precision floating-point number in Fauna.
     */
    Double,

    /**
     * Represents a UTF-8 encoded string in Fauna.
     */
    String,

    /**
     * Represents a date without time in Fauna.
     * Dates are in ISO 8601 format (YYYY-MM-DD).
     */
    Date,

    /**
     * Represents an exact timestamp or time value in Fauna.
     * Timestamps are in ISO 8601 format.
     */
    Time,

    /**
     * Represents a boolean value in Fauna.
     */
    Boolean,

    /**
     * Represents an object in Fauna.
     */
    Object,

    /**
     * Represents a reference to a document.
     */
    Ref,

    /**
     * Represents a complete document within Fauna.
     */
    Document,

    /**
     * Represents an array (or list) of values in Fauna.
     * Arrays are ordered collections of elements and can contain multiple data types.
     */
    Array,

    /**
     * Represents binary data encoded in Base64 within Fauna.
     * Used for storing raw bytes of data.
     */
    Bytes,

    /**
     * Represents a null value in Fauna, denoting the absence of a value.
     */
    Null,

    /**
     * Represents a stream type in Fauna, primarily used for real-time data streams.
     * Streams allow clients to receive data changes as they occur.
     */
    Stream,

    /**
     * Represents a module in Fauna Query Language (FQL), which serves as a symbolic object
     * with associated methods
     */
    Module,

    /**
     * Represents a pageable set of query results in Fauna.
     */
    Set
}
