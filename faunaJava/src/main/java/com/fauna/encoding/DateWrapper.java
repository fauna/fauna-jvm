package com.fauna.encoding;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;

/**
 * A wrapper class that formats {@link LocalDate} objects for serialization with GSON.
 * The {@code DateWrapper} class provides a convenient way to serialize Java {@link LocalDate}
 * instances into a JSON format expected by Fauna, where date values are represented as strings
 * with a specific annotation to be recognized by the database's query language.
 */
class DateWrapper {

    /**
     * The {@link LocalDate} value represented as a string.
     * It is annotated with {@code @SerializedName} to indicate the corresponding JSON key
     * when serialized with GSON.
     */
    @SerializedName("@date")
    private final String value;

    /**
     * Constructs a new {@code DateWrapper} instance using the provided {@link LocalDate} value.
     * The local date is converted to its ISO-8601 string representation.
     *
     * @param value The local date value to be wrapped. It should not be {@code null}.
     *              The date is formatted as an ISO-8601 string (such as "2007-12-03").
     *              If {@code null} is passed, it will result in a {@link NullPointerException}.
     * @throws NullPointerException If the {@code value} parameter is {@code null}.
     */
    public DateWrapper(LocalDate value) {
        if (value == null) {
            throw new NullPointerException("The LocalDate value for DateWrapper cannot be null.");
        }

        this.value = value.toString();
    }

}
