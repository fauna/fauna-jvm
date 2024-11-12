package com.fauna.codec;

import java.lang.reflect.Type;

/**
 * Interface for providing codecs. Responsible for obtaining codecs for specific classes and types.
 */
public interface CodecProvider {

    /**
     * Retrieves a codec for the specified class type.
     *
     * @param clazz The class type for which to obtain a codec.
     * @param <T>   The type of the class.
     * @return A codec capable of serializing and deserializing instances of the specified class.
     */
    <T> Codec<T> get(Class<T> clazz);

    /**
     * Retrieves a codec for the specified class type with additional type arguments for generic classes.
     *
     * @param clazz    The class type for which to obtain a codec.
     * @param typeArgs The generic type arguments, if applicable.
     * @param <T>      The type of the class.
     * @return A codec capable of serializing and deserializing instances of the specified class with the provided type arguments.
     */
    <T> Codec<T> get(Class<T> clazz, Type[] typeArgs);
}
