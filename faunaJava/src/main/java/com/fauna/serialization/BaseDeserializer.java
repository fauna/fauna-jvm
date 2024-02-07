package com.fauna.serialization;

public abstract class BaseDeserializer<T> implements IDeserializer<T> {

    @Override
    public Object deserializeNonGeneric(SerializationContext context, FaunaParser parser) {
        return deserializeGeneric(context, parser);
    }

    public abstract T deserializeGeneric(SerializationContext context, FaunaParser parser);
}
