package com.fauna.codec;

import com.fauna.exception.CodecException;


public interface Codec<T> {

    T decode(UTF8FaunaParser parser) throws CodecException;

    void encode(UTF8FaunaGenerator gen, T obj) throws CodecException;

    Class<?> getCodecClass();

    FaunaType[] getSupportedTypes();
}
