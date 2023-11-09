package com.fauna.encoding;

import com.google.gson.annotations.SerializedName;

/**
 * A wrapper class that formats {@code Double} values for serialization with GSON.
 * The {@code DoubleWrapper} class encapsulates a double value to be represented as a string
 * for compatibility with the JSON format expected by Fauna, which requires specific typing
 * of numbers as doubles using an annotation.
 */
class DoubleWrapper {

    /**
     * The {@code Double} value represented as a string, annotated for JSON serialization.
     * The annotation {@code @SerializedName("@double")} specifies the key name to use when
     * this attribute is serialized to JSON, conforming to the expected Fauna JSON format.
     */
    @SerializedName("@double")
    private final String value;

    /**
     * Constructs a new {@code DoubleWrapper} instance from a {@code Double} value.
     * This constructor converts the {@code Double} value into its string representation.
     *
     * @param value The {@code Double} value to be wrapped, which should not be {@code null}.
     *              Conversion to string is done using the {@code Double.toString()} method.
     *              If {@code null} is passed, it will result in a {@link NullPointerException}.
     * @throws NullPointerException If {@code value} is {@code null}, to prevent the creation
     *                              of an invalid {@code DoubleWrapper} instance.
     */
    public DoubleWrapper(Double value) {
        if (value == null) {
            throw new NullPointerException("The Double value for DoubleWrapper cannot be null.");
        }
        this.value = value.toString();
    }

}
