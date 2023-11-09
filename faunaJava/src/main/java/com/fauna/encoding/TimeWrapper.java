package com.fauna.encoding;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * A wrapper class for serializing {@link LocalDateTime} instances as UTC strings with GSON,
 * annotated to match the JSON serialization format required by Fauna for time values.
 * This class formats the date and time with microsecond precision, appending a 'Z' to
 * indicate UTC.
 */
class TimeWrapper {

    /**
     * The formatter to use for converting {@code LocalDateTime} instances to strings.
     * This formatter will produce times in the format "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
     * which is compliant with the ISO-8601 standard for representation of dates and times.
     */
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");

    /**
     * The {@link LocalDateTime} value formatted as a string. The {@link SerializedName} annotation
     * specifies the key "@time" under which this value will be placed in the serialized JSON object.
     */
    @SerializedName("@time")
    private final String value;

    /**
     * Constructs a new {@code TimeWrapper} instance from a {@link LocalDateTime} value.
     * The local date and time is converted to UTC and formatted to a string according to the
     * pattern defined by {@code formatter}.
     *
     * @param value The local date and time value to be converted to a string and wrapped.
     *              The value must be non-null and will be converted to UTC time zone.
     * @throws NullPointerException If the {@code value} is {@code null}.
     */
    public TimeWrapper(LocalDateTime value) {
        if (value == null) {
            throw new NullPointerException("The LocalDateTime value for TimeWrapper cannot be null.");
        }

        // Convert LocalDateTime to OffsetDateTime with UTC as the time zone.
        OffsetDateTime offsetDateTime = value.atOffset(ZoneOffset.UTC);
        // Format the OffsetDateTime to the string representation using the custom formatter.
        this.value = offsetDateTime.format(formatter);
    }

}
