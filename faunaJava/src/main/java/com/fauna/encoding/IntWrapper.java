package com.fauna.encoding;

import com.google.gson.annotations.SerializedName;

/**
 * A wrapper class for serializing {@link Integer} values as strings with GSON, using a specified
 * serialization key that matches the Fauna convention for integers. This class ensures that
 * integer values are correctly formatted as JSON strings in the expected format when interacting
 * with Fauna, which uses special typing annotations for numbers.
 */
class IntWrapper {

    /**
     * The {@link Integer} value represented as a string for JSON serialization. The field is
     * marked with {@link SerializedName} to indicate to GSON the key name "@int" under which
     * the serialized value should be placed in the JSON object.
     */
    @SerializedName("@int")
    private final String value;

    /**
     * Constructs a new {@code IntWrapper} instance from an {@link Integer} value. The integer
     * is converted to its String representation, as required for serialization.
     *
     * @param value The integer value to be converted to a string and wrapped. Must not be {@code null}
     *              to prevent the creation of an invalid {@code IntWrapper} instance. If {@code null},
     *              a {@link NullPointerException} is thrown.
     * @throws NullPointerException if the input value is {@code null}.
     */
    public IntWrapper(Integer value) {
        if (value == null) {
            throw new NullPointerException("The Integer value for IntWrapper cannot be null.");
        }
        this.value = value.toString();
    }

}
