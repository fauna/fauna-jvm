package com.fauna.serialization;

import com.fauna.enums.FaunaTokenType;
import com.fauna.exception.ClientException;
import com.fauna.interfaces.IDeserializer;

import java.io.IOException;
import java.util.Optional;

public class OptionalDeserializer<T> extends BaseDeserializer<Optional<T>> {

    private final IDeserializer<T> inner;

    public OptionalDeserializer(IDeserializer<T> innerDeserializer) {
        inner = innerDeserializer;
    }

    @Override
    public Optional<T> doDeserialize(UTF8FaunaParser reader)
        throws IOException {

        if (reader.getCurrentTokenType() == FaunaTokenType.NULL) {
            return Optional.empty();
        }

        return Optional.of(inner.deserialize(reader));
    }
}