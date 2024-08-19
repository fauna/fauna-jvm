package com.fauna.serialization;


import com.fauna.codec.ParameterizedOf;
import com.fauna.types.Document;
import com.fauna.types.DocumentRef;
import com.fauna.types.Module;
import com.fauna.types.NamedDocument;
import com.fauna.types.NamedDocumentRef;
import com.fauna.types.Page;
import com.fauna.interfaces.IDeserializer;
import com.fauna.mapping.MappingContext;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents methods for deserializing objects to and from Fauna's value format.
 */

public class Deserializer {

    private static final IDeserializer<Integer> _integer = new CheckedDeserializer(Integer.class);
    private static final IDeserializer<Byte> _byte = new CheckedDeserializer(Byte.class);
    private static final IDeserializer<Short> _short = new CheckedDeserializer(Short.class);
    private static final IDeserializer<String> _string = new CheckedDeserializer(String.class);
    private static final IDeserializer<LocalDate> _date = new CheckedDeserializer(LocalDate.class);
    private static final IDeserializer<Instant> _time = new CheckedDeserializer(Instant.class);
    private static final IDeserializer<Float> _float = new CheckedDeserializer(Float.class);
    private static final IDeserializer<Double> _double = new CheckedDeserializer(Double.class);
    private static final IDeserializer<Long> _long = new CheckedDeserializer(Long.class);
    private static final IDeserializer<Boolean> _boolean = new CheckedDeserializer(Boolean.class);
    private static final IDeserializer<Character> _char = new CheckedDeserializer<>(Character.class);
    private static final IDeserializer<Module> _module = new CheckedDeserializer(Module.class);
    private static final IDeserializer<Document> _document = new CheckedDeserializer(
        Document.class);
    private static final IDeserializer<NamedDocument> _namedDocument = new CheckedDeserializer(
        NamedDocument.class);
    private static final IDeserializer<DocumentRef> _documentRef = new CheckedDeserializer(
        DocumentRef.class);
    private static final IDeserializer<NamedDocumentRef> _namedDocumentRef = new CheckedDeserializer(
        NamedDocumentRef.class);
    private static final Map<Class<?>, IDeserializer<?>> DESERIALIZERS = new HashMap<>();

    static {
        DESERIALIZERS.put(Byte.class, _byte);
        DESERIALIZERS.put(byte.class, _byte);
        DESERIALIZERS.put(Short.class, _short);
        DESERIALIZERS.put(short.class, _short);
        DESERIALIZERS.put(Integer.class, _integer);
        DESERIALIZERS.put(int.class, _integer);
        DESERIALIZERS.put(long.class, _long);
        DESERIALIZERS.put(Long.class, _long);
        DESERIALIZERS.put(String.class, _string);
        DESERIALIZERS.put(LocalDate.class, _date);
        DESERIALIZERS.put(Instant.class, _time);
        DESERIALIZERS.put(Float.class, _float);
        DESERIALIZERS.put(float.class, _float);
        DESERIALIZERS.put(double.class, _double);
        DESERIALIZERS.put(Double.class, _double);
        DESERIALIZERS.put(boolean.class, _boolean);
        DESERIALIZERS.put(Boolean.class, _boolean);
        DESERIALIZERS.put(char.class, _char);
        DESERIALIZERS.put(Character.class, _char);
        DESERIALIZERS.put(Module.class, _module);
        DESERIALIZERS.put(Document.class, _document);
        DESERIALIZERS.put(NamedDocument.class, _namedDocument);
        DESERIALIZERS.put(DocumentRef.class, _documentRef);
        DESERIALIZERS.put(NamedDocumentRef.class, _namedDocumentRef);
        DESERIALIZERS.put(Object.class, DynamicDeserializer.getInstance());
    }

    /**
     * The dynamic data deserializer.
     */
    public static final IDeserializer<Object> DYNAMIC = DynamicDeserializer.getInstance();

    /**
     * Generates a deserializer for the specified non-nullable Java type.
     *
     * @param <T>     The type of the object to deserialize to.
     * @param context The serialization context.
     * @param type    The Java type to generate a deserializer for.
     * @return An {@code IDeserializer<T>}.
     */
    public static <T> IDeserializer<T> generate(MappingContext context, Type type) {
        IDeserializer<?> deser = generateImpl(context, type);
        return castDeserializer(deser);
    }

    /**
     * Generates a deserializer which returns values of the specified Java type, or the default if
     * the underlying query value is null.
     *
     * @param <T>     The type of the object to deserialize to.
     * @param context The serialization context.
     * @param type    The Java type to generate a deserializer for.
     * @return An {@code IDeserializer<T>}.
     */
    public static <T> IDeserializer<T> generateNullable(MappingContext context, Type type) {
        IDeserializer<T> deser = generate(context, type);
        return wrapNullable(deser);
    }

    @SuppressWarnings("unchecked")
    private static <T> IDeserializer<T> castDeserializer(IDeserializer<?> deser) {
        return (IDeserializer<T>) deser;
    }

    private static <T> IDeserializer<T> wrapNullable(IDeserializer<T> deser) {
        return new NullableDeserializer<>(deser);
    }

    private static <T> IDeserializer<T> generateImpl(MappingContext context, Type type) {

        if (DESERIALIZERS.containsKey(type)) {
            IDeserializer<?> deserializer = DESERIALIZERS.get(type);
            if (deserializer != null) {
                return (IDeserializer<T>) deserializer;
            }
        } else if (type instanceof ParameterizedOf) {
            ParameterizedOf parameterizedType = (ParameterizedOf) type;
            Type rawType = parameterizedType.getRawType();
            if (rawType == Map.class) {
                Type[] typeArgs = parameterizedType.getActualTypeArguments();
                Type keyType = typeArgs[0];
                Type valueType = typeArgs[1];

                if (keyType != String.class) {
                    throw new IllegalArgumentException(
                        "Unsupported Map key type. Key must be of type String, but was " + keyType);
                }

                IDeserializer<?> valueDeserializer = generate(context, valueType);

                return (IDeserializer<T>) new MapDeserializer<>(valueDeserializer);

            }
            if (rawType == List.class || rawType == Page.class) {
                Type[] typeArgs = parameterizedType.getActualTypeArguments();
                Type elemType = typeArgs[0];
                IDeserializer<?> elemDeserializer = generate(context, elemType);

                if (rawType == List.class) {
                    return (IDeserializer<T>) new ListDeserializer<>(elemDeserializer);
                } else if (rawType == Page.class) {
                    return (IDeserializer<T>) new PageDeserializer<>(elemDeserializer);
                }
            }

            if (rawType == Optional.class) {
                Type[] typeArgs = parameterizedType.getActualTypeArguments();
                Type elemType = typeArgs[0];
                IDeserializer<?> elemDeserializer = generate(context, elemType);
                return (IDeserializer<T>) new OptionalDeserializer<>(elemDeserializer);
            }
        } else if (type instanceof Class<?> && !DESERIALIZERS.containsKey(type)
            && context != null) {
            return (IDeserializer<T>) context.getInfo(type).getDeserializer();
        }
        throw new IllegalArgumentException(
            "Unsupported deserialization target type " + type.getTypeName());


    }
}