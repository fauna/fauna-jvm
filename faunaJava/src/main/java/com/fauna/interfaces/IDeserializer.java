package com.fauna.interfaces;


import com.fauna.serialization.FaunaParser;
import com.fauna.serialization.SerializationContext;
import java.io.IOException;

public interface IDeserializer<T> {

    T deserialize(SerializationContext context, FaunaParser parser) throws IOException;

}

