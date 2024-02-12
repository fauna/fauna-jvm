package com.fauna.serialization;


import java.time.Instant;
import java.time.LocalDate;

public class Deserializer {

    public static final IDeserializer<Integer> _integer = DynamicDeserializer.getInstance(
        Integer.class);
    public static final IDeserializer<String> _string = DynamicDeserializer.getInstance(
        String.class);
    public static final IDeserializer<LocalDate> _date = DynamicDeserializer.getInstance(
        LocalDate.class);
    public static final IDeserializer<Instant> _time = DynamicDeserializer.getInstance(
        Instant.class);
    public static final IDeserializer<Double> _double = DynamicDeserializer.getInstance(
        Double.class);
    public static final IDeserializer<Long> _long = DynamicDeserializer.getInstance(
        Long.class);
    public static final IDeserializer<Boolean> _boolean = DynamicDeserializer.getInstance(
        Boolean.class);

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