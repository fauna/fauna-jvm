package com.fauna.serialization;


import com.fauna.common.types.Module;
import com.google.common.reflect.TypeToken;
import java.lang.reflect.Type;
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

    /**
     * The dynamic data deserializer.
     */
    public static final IDeserializer<Object> DYNAMIC = DynamicDeserializer.getInstance();

    /**
     * Generates a deserializer for the specified non-nullable Java type.
     *
     * @param <T>     The type of the object to deserialize to.
     * @param context The serialization context.
     * @return An {@code IDeserializer<T>}.
     */
    public static <T> IDeserializer<T> generate(SerializationContext context,
        TypeToken<T> targetTypeToken) {
        IDeserializer<?> deser = generateImpl(context, targetTypeToken);
        return castDeserializer(deser);
    }

    /**
     * Generates a deserializer for the specified non-nullable Java type.
     *
     * @param <T>     The type of the object to deserialize to.
     * @param context The serialization context.
     * @return An {@code IDeserializer<T>}.
     */
    public static <T> IDeserializer<T> generate(SerializationContext context,
        Class<T> targetClass) {
        IDeserializer<?> deser = generateImpl(context, targetClass);
        return castDeserializer(deser);
    }

    /**
     * Generates a deserializer which returns values of the specified Java type, or the default if
     * the underlying query value is null.
     *
     * @param <T>     The type of the object to deserialize to.
     * @param context The serialization context.
     * @return An {@code IDeserializer<T>}.
     */
    public static <T> IDeserializer<T> generateNullable(SerializationContext context,
        Class<T> targetTypeToken) {
        IDeserializer<T> deser = generate(context, targetTypeToken);
        return wrapNullable(deser);
    }

    /**
     * Generates a deserializer which returns values of the specified Java type, or the default if
     * the underlying query value is null.
     *
     * @param <T>     The type of the object to deserialize to.
     * @param context The serialization context.
     * @return An {@code IDeserializer<T>}.
     */
    public static <T> IDeserializer<T> generateNullable(SerializationContext context,
        TypeToken<T> targetTypeToken) {
        IDeserializer<T> deser = generate(context, targetTypeToken);
        return wrapNullable(deser);
    }

    @SuppressWarnings("unchecked")
    private static <T> IDeserializer<T> castDeserializer(IDeserializer<?> deser) {
        return (IDeserializer<T>) deser;
    }

    private static <T> IDeserializer<T> wrapNullable(IDeserializer<T> deser) {
        return new NullableDeserializer<>(deser);
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

        throw new IllegalArgumentException(
            "Unsupported deserialization target type " + targetType.getName());
    }

    private static <T> IDeserializer<T> generateImpl(SerializationContext context,
        TypeToken<T> targetType) {
        Type type = targetType.getType();

        throw new IllegalArgumentException(
            "Unsupported deserialization target type " + type.getTypeName());
    }
}