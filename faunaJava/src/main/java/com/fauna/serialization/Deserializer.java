package com.fauna.serialization;


public class Deserializer {

    public static final IDeserializer<Integer> _integer = DynamicDeserializer.getInstance(
        Integer.class);
    public static final IDeserializer<String> _string = DynamicDeserializer.getInstance(
        String.class);

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

        throw new IllegalArgumentException(
            "Unsupported deserialization target type " + targetType.getName());
    }
}