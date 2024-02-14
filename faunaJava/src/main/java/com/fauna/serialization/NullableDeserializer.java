package com.fauna.serialization;

import com.fauna.common.enums.FaunaTokenType;
import com.fauna.interfaces.IDeserializer;
import java.io.IOException;

public class NullableDeserializer<T> extends BaseDeserializer<T> {

    private final IDeserializer<T> inner;

    public NullableDeserializer(IDeserializer<T> inner) {
        this.inner = inner;
    }

    @Override
    public T deserialize(SerializationContext context, FaunaParser reader) throws IOException {
        if (reader.getCurrentTokenType() == FaunaTokenType.NULL) {
            return null;
        }
        return inner.deserialize(context, reader);
    }
}
