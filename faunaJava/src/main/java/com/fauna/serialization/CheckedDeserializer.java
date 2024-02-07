package com.fauna.serialization;

import com.fauna.common.enums.FaunaTokenType;
import com.fauna.exception.SerializationException;

public class CheckedDeserializer<T> extends BaseDeserializer<T> {

    public T deserialize(SerializationContext context, FaunaParser parser) {
        FaunaTokenType tokenType = parser.getCurrentTokenType();
        Object obj = DynamicDeserializer.getInstance().deserialize(context, parser);

        if (obj instanceof Integer) {
            return (T) obj;
        } else {
            throw new SerializationException(
                "Unexpected token while deserializing: " + tokenType);
        }
    }

    @Override
    public T deserializeGeneric(SerializationContext context, FaunaParser parser) {
        return null;
    }
}