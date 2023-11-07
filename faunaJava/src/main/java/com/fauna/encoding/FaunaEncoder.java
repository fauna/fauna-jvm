package com.fauna.encoding;

import com.fauna.exception.TypeError;
import com.fauna.query.builder.Fragment;
import com.fauna.query.builder.LiteralFragment;
import com.fauna.query.builder.Query;
import com.fauna.query.builder.ValueFragment;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for encoding various Java types into a representation suitable for Fauna queries.
 * The encoder translates Java objects to their Fauna tag equivalents, ensuring type fidelity when storing or querying the database.
 * <p>
 * Supported type conversions are as follows:
 * <ul>
 *     <li>{@code Map} objects to {@code @object} for Fauna.</li>
 *     <li>{@code List} objects to arrays for Fauna.</li>
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
 *     <li>{@code Query} instances to {@code fql} for Fauna.</li>
 *     <li>{@code ValueFragment} instances to {@code value} for Fauna.</li>
 *     <li>And literal string representations of queries to {@code string} for Fauna.</li>
 * </ul>
 * <p>
 * This class ensures that data types are encoded properly to maintain the integrity of the data when interacting with the Fauna service.
 */
public class FaunaEncoder {

    private static final String INT_TAG = "@int";
    private static final String LONG_TAG = "@long";
    private static final String DOUBLE_TAG = "@double";
    private static final String DATE_TAG = "@date";
    private static final String TIME_TAG = "@time";
    private static final String REF_TAG = "@ref";
    private static final String MOD_TAG = "@mod";
    private static final String OBJECT_TAG = "@object";
    private static final String DOC_TAG = "@doc";

    private static final Set<String> RESERVED_TAGS = Set.of(
            INT_TAG, LONG_TAG, DOUBLE_TAG, DATE_TAG, TIME_TAG, MOD_TAG, DOC_TAG, REF_TAG, OBJECT_TAG
    );
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
        if (value instanceof Query) {
            return encodeQuery((Query) value);
        }
        if (value instanceof Map<?, ?>) {
            return encodeMap((Map<?, ?>) value);
        }
        if (value instanceof List<?>) {
            return encodeList((List<?>) value);
        }
        throw new TypeError("Unsupported type: " + value.getClass().getName());
    }

    /**
     * Encodes a list of objects for Fauna.
     * Iterates over the list and encodes each item using the wrapValue method.
     *
     * @param list The list of objects to encode.
     * @return A List containing encoded representations of the original list's items.
     */
    private static List<Object> encodeList(List<?> list) {
        List<Object> encodedList = new ArrayList<>(list.size());
        for (Object item : list) {
            encodedList.add(wrapValue(item));
        }
        return encodedList;
    }

    /**
     * Encodes a map for Fauna.
     * Checks for reserved keys and encodes each entry using the wrapValue method.
     * If reserved keys are present, the map is nested under an "@object" tag.
     *
     * @param map The map to encode.
     * @return A Map containing the encoded representation of the original map.
     * @throws IllegalArgumentException if the map contains keys that are not strings.
     */
    private static Map<String, Object> encodeMap(Map<?, ?> map) {
        Map<String, Object> encodedMap = new HashMap<>();
        boolean hasReservedKeys = false; // Flag to track if any reserved keys are present

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            if (!(key instanceof String keyStr)) {
                throw new IllegalArgumentException("Map keys must be strings to encode as Fauna object");
            }
            if (RESERVED_TAGS.contains(keyStr)) {
                hasReservedKeys = true; // Set flag if reserved key is found
                encodedMap.put(keyStr, wrapValue(entry.getValue())); // Directly add to encodedMap for now
            } else {
                encodedMap.put(keyStr, wrapValue(entry.getValue()));
            }
        }

        // If reserved keys were present, nest all keys under OBJECT_TAG
        if (hasReservedKeys) {
            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put(OBJECT_TAG, encodedMap); // Create a nested map for reserved keys
            return objectMap; // Return the nested map
        }

        return encodedMap;
    }

    /**
     * Encodes a Query object into a representation suitable for Fauna.
     *
     * @param value the Query object to encode.
     * @return a Map representation of the query for Fauna.
     */
    private static Map<String, List<Object>> encodeQuery(Query value) {
        return Collections.singletonMap("fql",
                value.getFragments().stream()
                        .map(FaunaEncoder::encodeFragment)
                        .collect(Collectors.toList()));
    }

    /**
     * Encodes a Fragment object.
     *
     * @param value the Fragment to encode.
     * @return an Object representation of the fragment.
     * @throws IllegalArgumentException if the fragment type is unknown.
     */
    private static Object encodeFragment(Fragment value) {
        if (value instanceof LiteralFragment) {
            return ((LiteralFragment) value).get();
        }
        if (value instanceof ValueFragment) {
            Object fragmentValue = value.get();
            if (fragmentValue instanceof Query) {
                return encodeQuery((Query) fragmentValue);
            }
            return Collections.singletonMap("value", wrapValue(fragmentValue));
        }
        throw new IllegalArgumentException("Unknown fragment type: " + value.getClass());

    }

}
