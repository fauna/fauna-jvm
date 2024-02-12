package com.fauna.serialization;


import java.time.Instant;
import java.time.LocalDate;

public class Deserializer {

    public static final IDeserializer<Integer> _integer = new CheckedDeserializer(Integer.class);
    public static final IDeserializer<String> _string = new CheckedDeserializer(String.class);
    public static final IDeserializer<LocalDate> _date = new CheckedDeserializer(LocalDate.class);
    public static final IDeserializer<Instant> _time = new CheckedDeserializer(Instant.class);
    public static final IDeserializer<Double> _double = new CheckedDeserializer(Double.class);
    public static final IDeserializer<Long> _long = new CheckedDeserializer(Long.class);
    public static final IDeserializer<Boolean> _boolean = new CheckedDeserializer(Boolean.class);

    public static final IDeserializer<Object> DYNAMIC = DynamicDeserializer.getInstance();

    public static <T> IDeserializer<T> generate(SerializationContext context, Class<T> targetType) {
        IDeserializer<?> deser = generateImpl(context, targetType);
        return (IDeserializer<T>) deser;
    }

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

        throw new IllegalArgumentException(
            "Unsupported deserialization target type " + targetType.getName());
    }
}