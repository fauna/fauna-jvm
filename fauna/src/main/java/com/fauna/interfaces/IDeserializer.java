package com.fauna.interfaces;


import com.fauna.mapping.MappingContext;
import com.fauna.serialization.UTF8FaunaParser;
import java.io.IOException;

public interface IDeserializer<T> {

    T deserialize(MappingContext context, UTF8FaunaParser parser) throws IOException;

}

