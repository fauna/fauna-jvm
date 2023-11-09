package com.fauna.encoding;

import com.fauna.exception.TypeError;
import com.fauna.query.model.Document;
import com.fauna.query.model.DocumentReference;
import com.fauna.query.model.Module;
import com.fauna.query.model.NamedDocument;
import com.fauna.query.model.NamedDocumentReference;
import com.fauna.query.model.NullDocument;
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
 *     <li>{@code null} values are preserved as {@code None} for Fauna.</li>
 *     <li>{@code Document} instances to {@code @ref} for Fauna.</li>
 *     <li>{@code DocumentReference} instances to {@code @ref} for Fauna.</li>
 *     <li>{@code Module} instances to {@code @mod} for Fauna.</li>
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
     * @throws TypeError if the object type is not supported by the encoder.
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
        if (value instanceof DocumentReference) {
            return new DocumentReferenceWrapper((DocumentReference) value);
        }
        if (value instanceof NamedDocumentReference) {
            return new NamedDocumentReferenceWrapper((NamedDocumentReference) value);
        }
        if (value instanceof Module) {
            return new ModuleWrapper((Module) value);
        }
        if (value instanceof NullDocument) {
            return new NullDocumentWrapper((NullDocument) value);
        }
        if (value instanceof Document) {
            return new DocumentReferenceWrapper(
                    new DocumentReference(((Document) value).getColl(), ((Document) value).getId())
            );
        }
        if (value instanceof NamedDocument) {
            return new NamedDocumentReferenceWrapper(
                    new NamedDocumentReference(((NamedDocument) value).getColl(), ((NamedDocument) value).getName())
            );
        }
        throw new TypeError("Unsupported type: " + value.getClass().getName());
    }

}
