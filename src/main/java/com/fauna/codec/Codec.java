package com.fauna.codec;

import java.io.IOException;


public interface Codec<T> {

    T decode(UTF8FaunaParser parser) throws IOException;
    void encode(UTF8FaunaGenerator gen, T obj) throws IOException;
    Class<?> getCodecClass();

    FaunaType[] getSupportedTypes();
}
