package com.fauna.codec;

import com.fauna.mapping.MappingContext;
import com.fauna.serialization.UTF8FaunaGenerator;
import com.fauna.serialization.UTF8FaunaParser;

import java.io.IOException;


public interface Codec<T> {

    T decode(UTF8FaunaParser parser) throws IOException;

    void encode(UTF8FaunaGenerator gen, T obj, MappingContext context) throws IOException;

    Class<T> getCodecClass();
}
