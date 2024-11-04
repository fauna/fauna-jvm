package com.fauna.codec;

public interface CodecRegistry {

    <T> Codec<T> get(CodecRegistryKey key);

    <T> void put(CodecRegistryKey key, Codec<T> codec);

    boolean contains(CodecRegistryKey key);
}
