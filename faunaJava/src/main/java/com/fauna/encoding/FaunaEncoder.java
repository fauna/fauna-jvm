package com.fauna.encoding;

import com.fauna.exception.TypeError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Utility class for encoding various Java types into a representation suitable for Fauna queries.
 * The encoder translates Java objects to their Fauna tag equivalents, ensuring type fidelity when storing or querying the database.
 * <p>
 * Supported type conversions are as follows:
 * <ul>
 *     <li>{@code String} objects to strings for Fauna.</li>
 *     <li>{@code Integer} values (32-bit signed) to {@code @int} for Fauna.</li>
 *     <li>{@code Long} values (64-bit signed) to {@code @long} for Fauna.</li>
 *     <li>{@code Double} values to {@code @double} for Fauna.</li>
 *     <li>{@code LocalDateTime} values to {@code @time} for Fauna.</li>
 *     <li>{@code LocalDate} values to {@code @date} for Fauna.</li>
 *     <li>{@code Boolean} values {@code true} and {@code false} are preserved as is for Fauna.</li>
 * </ul>
 * <p>
 * This class ensures that data types are encoded properly to maintain the integrity of the data when interacting with the Fauna service.
 */
public class FaunaEncoder {

    private static final Gson gson = new GsonBuilder().serializeNulls().create();

    private FaunaEncoder() {
    }

    /**
     * Encodes an object into its corresponding Fauna representation.
     * This method dispatches the object to the appropriate encoder method based on its type.
     *
     * @param value The object to encode.
     * @return A string containing the JSON encoded representation of the value.
     */
    public static String encode(Object value) {
        return gson.toJson(wrapValue(value));
    }

    /**
     * Wraps a value in its Fauna representation.
     * This method handles encoding of basic types, special Fauna types, and structures like lists and maps.
     *
     * @param value The value to wrap.
     * @return The encoded representation of the value.
     */
    private static Object wrapValue(Object value) {
        // This method decides how to wrap the value based on its type
        if (value instanceof String || value == null || value instanceof Boolean) {
            return value;
        }
        if (value instanceof Integer) {
            return new IntWrapper((Integer) value);
        }
        if (value instanceof Long) {
            return new LongWrapper((Long) value);
        }
        if (value instanceof Double) {
            return new DoubleWrapper((Double) value);
        }
        if (value instanceof LocalDateTime) {
            return new TimeWrapper((LocalDateTime) value);
        }
        if (value instanceof LocalDate) {
            return new DateWrapper((LocalDate) value);
        }
        throw new TypeError("Unsupported type: " + value.getClass().getName());
    }

}
