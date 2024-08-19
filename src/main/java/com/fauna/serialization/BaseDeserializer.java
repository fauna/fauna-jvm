package com.fauna.serialization;

import com.fauna.codec.UTF8FaunaParser;
import com.fauna.interfaces.IDeserializer;

import java.io.IOException;

public abstract class BaseDeserializer<T> implements IDeserializer<T> {

    @Override
    public T deserialize(UTF8FaunaParser parser) throws IOException {
        return doDeserialize(parser);
    }

    protected abstract T doDeserialize(UTF8FaunaParser parser)
        throws IOException;
}
