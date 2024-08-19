package com.fauna.serialization;

import com.fauna.codec.UTF8FaunaParser;
import com.fauna.enums.FaunaTokenType;
import com.fauna.interfaces.IDeserializer;

import java.io.IOException;

public class NullableDeserializer<T> extends BaseDeserializer<T> {

    private final IDeserializer<T> inner;

    public NullableDeserializer(IDeserializer<T> inner) {
        this.inner = inner;
    }

    @Override
    public T doDeserialize(UTF8FaunaParser reader) throws IOException {
        if (reader.getCurrentTokenType() == FaunaTokenType.NULL) {
            return null;
        }
        return inner.deserialize(reader);
    }
}
