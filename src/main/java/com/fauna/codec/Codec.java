package com.fauna.codec;

import com.fauna.exception.CodecException;

/**
 * Interface for codecs, which handle the serialization and deserialization of specific types.
 * <p>
 * Each codec is associated with a particular class and supports a set of Fauna data types.
 *
 * @param <T> The type of object that this codec can encode and decode.
 */
public interface Codec<T> {

    /**
     * Decodes an object from the provided {@link UTF8FaunaParser}.
     *
     * @param parser The parser to use for reading and decoding the data.
     * @return The decoded object of type {@code T}.
     * @throws CodecException If an error occurs during decoding.
     */
    T decode(UTF8FaunaParser parser) throws CodecException;

    /**
     * Encodes the specified object using the provided {@link UTF8FaunaGenerator}.
     *
     * @param gen The generator to use for writing and encoding the data.
     * @param obj The object of type {@code T} to encode.
     * @throws CodecException If an error occurs during encoding.
     */
    void encode(UTF8FaunaGenerator gen, T obj) throws CodecException;

    /**
     * Gets the class associated with this codec.
     *
     * @return The {@link Class} type that this codec handles.
     */
    Class<?> getCodecClass();

    /**
     * Gets the set of supported Fauna data types for this codec.
     *
     * @return An array of {@link FaunaType} values representing the supported types.
     */
    FaunaType[] getSupportedTypes();
}
