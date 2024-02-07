package com.fauna.serialization;


public class Deserializer {

    public static final IDeserializer<Object> instance = DynamicDeserializer.getInstance();

    public static <T> IDeserializer<T> generate(SerializationContext context, Class<T> targetType) {
        IDeserializer<?> deser = generateImpl(context, targetType);
        return (IDeserializer<T>) deser;
    }

    private static IDeserializer<?> generateImpl(SerializationContext context,
        Class<?> targetType) {
        if (targetType == Integer.class || targetType == int.class) {
            return new CheckedDeserializer<>();
        }

        throw new IllegalArgumentException(
            "Unsupported deserialization target type " + targetType.getName());
    }
}