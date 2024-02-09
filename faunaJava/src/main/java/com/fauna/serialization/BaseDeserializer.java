package com.fauna.serialization;

public abstract class BaseDeserializer<T> implements IDeserializer<T> {

    public abstract T deserialize(SerializationContext context, FaunaParser parser);
}
