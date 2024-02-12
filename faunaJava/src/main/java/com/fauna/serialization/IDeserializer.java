package com.fauna.serialization;


import java.io.IOException;

public interface IDeserializer<T> {

    T deserialize(SerializationContext context, FaunaParser parser) throws IOException;

}
