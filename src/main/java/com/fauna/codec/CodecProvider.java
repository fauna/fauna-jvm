package com.fauna.codec;

public interface CodecProvider {
    <T> Codec<T> get(CodecRegistry registry, Class<T> clazz);
    <T,K> Codec<T> get(CodecRegistry registry, Class<T> clazz, Class<K> subClazz);
}
