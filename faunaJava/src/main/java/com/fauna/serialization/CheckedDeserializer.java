package com.fauna.serialization;

import com.fauna.exception.SerializationException;

public class CheckedDeserializer<T> extends BaseDeserializer<T> {

    private final Class<T> type;

    public CheckedDeserializer(Class<T> type) {
        this.type = type;
    }

    public T deserialize(SerializationContext context, FaunaParser reader) {
        Object tokenType = reader.getCurrentTokenType();
        Object obj = DynamicDeserializer.getInstance(type).deserialize(context, reader);

        if (type.isInstance(obj)) {
            return type.cast(obj);
        } else {
            throw new SerializationException(
                "Unexpected token while deserializing: " + tokenType);
        }
    }
}