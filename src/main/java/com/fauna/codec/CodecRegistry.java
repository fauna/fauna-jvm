package com.fauna.codec;

/**
 * Interface defining a registry for codecs, which manage the serialization and deserialization of objects.
 * <p>
 * Provides methods for storing, retrieving, and checking for codecs by their unique keys.
 */
public interface CodecRegistry {

    /**
     * Retrieves the codec associated with the specified key.
     *
     * @param key The unique key representing the codec.
     * @param <T> The type of the object handled by the codec.
     * @return The codec associated with the specified key, or {@code null} if not found.
     */
    <T> Codec<T> get(CodecRegistryKey key);

    /**
     * Registers a codec with the specified key in the registry.
     *
     * @param key   The unique key representing the codec.
     * @param codec The codec to register.
     * @param <T>   The type of the object handled by the codec.
     */
    <T> void put(CodecRegistryKey key, Codec<T> codec);

    /**
     * Checks if a codec is registered under the specified key.
     *
     * @param key The unique key representing the codec.
     * @return {@code true} if a codec exists for the specified key; {@code false} otherwise.
     */
    boolean contains(CodecRegistryKey key);
}
