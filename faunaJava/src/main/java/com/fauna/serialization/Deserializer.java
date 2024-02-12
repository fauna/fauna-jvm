package com.fauna.serialization;


import com.fauna.common.types.Document;
import com.fauna.common.types.DocumentRef;
import com.fauna.common.types.Module;
import com.fauna.common.types.NamedDocument;
import com.fauna.common.types.NamedDocumentRef;
import com.fauna.common.types.NullDocumentRef;
import com.fauna.common.types.NullNamedDocumentRef;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Represents methods for deserializing objects to and from Fauna's value format.
 */

public class Deserializer {

    private static final IDeserializer<Integer> _integer = new CheckedDeserializer(Integer.class);
    private static final IDeserializer<String> _string = new CheckedDeserializer(String.class);
    private static final IDeserializer<LocalDate> _date = new CheckedDeserializer(LocalDate.class);
    private static final IDeserializer<Instant> _time = new CheckedDeserializer(Instant.class);
    private static final IDeserializer<Double> _double = new CheckedDeserializer(Double.class);
    private static final IDeserializer<Long> _long = new CheckedDeserializer(Long.class);
    private static final IDeserializer<Boolean> _boolean = new CheckedDeserializer(Boolean.class);
    private static final IDeserializer<Module> _module = new CheckedDeserializer(Module.class);
    private static final IDeserializer<Document> _document = new CheckedDeserializer(
        Document.class);
    private static final IDeserializer<NamedDocument> _namedDocument = new CheckedDeserializer(
        NamedDocument.class);
    private static final IDeserializer<DocumentRef> _documentRef = new CheckedDeserializer(
        DocumentRef.class);
    private static final IDeserializer<NamedDocumentRef> _namedDocumentRef = new CheckedDeserializer(
        NamedDocumentRef.class);
    private static final IDeserializer<NullDocumentRef> _nullDocumentRef = new CheckedDeserializer(
        NullDocumentRef.class);
    private static final IDeserializer<NullNamedDocumentRef> _nullNamedDocumentRef = new CheckedDeserializer(
        NullNamedDocumentRef.class);

    /**
     * The dynamic data deserializer.
     */
    public static final IDeserializer<Object> DYNAMIC = DynamicDeserializer.getInstance();

    /**
     * Generates a deserializer for the specified non-nullable .NET type.
     *
     * @param <T>     The type of the object to deserialize to.
     * @param context The serialization context.
     * @return An {@code IDeserializer<T>}.
     */
    public static <T> IDeserializer<T> generate(SerializationContext context, Class<T> targetType) {
        IDeserializer<?> deser = generateImpl(context, targetType);
        return (IDeserializer<T>) deser;
    }

    /**
     * Generates a deserializer which returns values of the specified .NET type, or the default if
     * the underlying query value is null.
     *
     * @param <T>     The type of the object to deserialize to.
     * @param context The serialization context.
     * @return An {@code IDeserializer<T>}.
     */
    public static <T> IDeserializer<T> generateNullable(SerializationContext context,
        Class<T> targetType) {
        IDeserializer<T> deser = generate(context, targetType);
        NullableDeserializer<T> nullable = new NullableDeserializer<>(deser);
        return nullable;
    }

    private static <T> IDeserializer<T> generateImpl(SerializationContext context,
        Class<T> targetType) {
        if (targetType == Integer.class || targetType == int.class) {
            return (IDeserializer<T>) _integer;
        }
        if (targetType == String.class) {
            return (IDeserializer<T>) _string;
        }
        if (targetType == LocalDate.class) {
            return (IDeserializer<T>) _date;
        }
        if (targetType == Instant.class) {
            return (IDeserializer<T>) _time;
        }
        if (targetType == double.class || targetType == Double.class) {
            return (IDeserializer<T>) _double;
        }
        if (targetType == long.class || targetType == Long.class) {
            return (IDeserializer<T>) _long;
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return (IDeserializer<T>) _boolean;
        }
        if (targetType == Module.class) {
            return (IDeserializer<T>) _module;
        }
        if (targetType == Document.class) {
            return (IDeserializer<T>) _document;
        }
        if (targetType == NamedDocument.class) {
            return (IDeserializer<T>) _namedDocument;
        }
        if (targetType == DocumentRef.class) {
            return (IDeserializer<T>) _documentRef;
        }
        if (targetType == NullDocumentRef.class) {
            return (IDeserializer<T>) _nullDocumentRef;
        }
        if (targetType == NamedDocumentRef.class) {
            return (IDeserializer<T>) _namedDocumentRef;
        }
        if (targetType == NullNamedDocumentRef.class) {
            return (IDeserializer<T>) _nullNamedDocumentRef;
        }

        throw new IllegalArgumentException(
            "Unsupported deserialization target type " + targetType.getName());
    }
}