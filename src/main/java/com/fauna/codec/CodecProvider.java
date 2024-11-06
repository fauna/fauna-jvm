package com.fauna.codec;

import java.lang.reflect.Type;

public interface CodecProvider {
    <T> Codec<T> get(Class<T> clazz);

    <T> Codec<T> get(Class<T> clazz, Type[] typeArgs);
}
