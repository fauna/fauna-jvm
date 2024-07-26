package com.fauna.codec;

import com.fauna.mapping.MappingContext;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents methods for encoding and decoding objects to and from Fauna's value format.
 */

public class CodecProvider {

    private static final Map<Class<?>, Codec<?>> CODECS = new HashMap<>();


    /**
     * Generates a deserializer for the specified non-nullable Java type.
     *
     * @param <T>     The type of the object to deserialize to.
     * @param context The serialization context.
     * @param type    The Java type to generate a deserializer for.
     * @return An {@code Codec<T>}.
     */
    public static <T> Codec<T> generate(MappingContext context, Type type) {
        Codec<?> deser = generateImpl(context, type);
        return castCodec(deser);
    }


    @SuppressWarnings("unchecked")
    private static <T> Codec<T> castCodec(Codec<?> codec) {
        return (Codec<T>) codec;
    }

    private static <T> Codec<T> generateImpl(MappingContext context, Type type) {

        if (CODECS.containsKey(type)) {
            Codec<?> codec = CODECS.get(type);
            if (codec != null) {
                return (Codec<T>) codec;
            }
        }

        return DynamicCodec.getInstance();
    }
}