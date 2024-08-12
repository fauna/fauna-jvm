package com.fauna.codec;

public interface CodecProvider {
    <T> Codec<T> get(Class<T> clazz);
    <T,E> Codec<T> get(Class<T> clazz, Class<E> typeArg);
}
