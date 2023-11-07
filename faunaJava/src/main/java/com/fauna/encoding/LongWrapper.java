package com.fauna.encoding;

import com.google.gson.annotations.SerializedName;

/**
 * A wrapper class for serializing {@link Long} values as strings with GSON, conforming to the
 * expected JSON serialization format for Fauna. When Fauna expects a number to be explicitly
 * typed as a long, this class ensures that {@link Long} values are serialized with the correct
 * type annotation.
 */
class LongWrapper {

    /**
     * The {@link Long} value represented as a string for JSON serialization. It is annotated with
     * {@link SerializedName} to specify the "@long" key that Fauna uses to recognize the type of
     * the serialized number.
     */
    @SerializedName("@long")
    private final String value;

    /**
     * Constructs a new {@code LongWrapper} with the given {@link Long} value by converting it to
     * its string representation, as Fauna expects long values to be transmitted as strings in
     * JSON objects.
     *
     * @param value The {@link Long} value to be wrapped, which should not be {@code null} to
     *              ensure proper serialization. If {@code null} is provided, the constructor will
     *              throw a {@link NullPointerException}.
     * @throws NullPointerException If the input value is {@code null}.
     */
    public LongWrapper(Long value) {
        if (value == null) {
            throw new NullPointerException("The Long value for LongWrapper cannot be null.");
        }
        this.value = value.toString();
    }

}
