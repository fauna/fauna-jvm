package com.fauna.codec;

import com.fauna.mapping.MappingContext;
import com.fauna.mapping.MappingInfo;
import com.fauna.serialization.UTF8FaunaGenerator;
import com.fauna.serialization.UTF8FaunaParser;

import java.io.IOException;

public class ClassCodec<T> implements Codec<T> {

    private final MappingInfo info;

    public ClassCodec(MappingInfo info) {
        this.info = info;
    }

    @Override
    public T decode(UTF8FaunaParser parser) throws IOException {
        return null;
    }

    @Override
    public void encode(UTF8FaunaGenerator writer, T obj, MappingContext context) throws IOException {

    }

    @Override
    public Class<T> getCodecClass() {
        return null;
    }
}
