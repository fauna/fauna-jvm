package com.fauna.interfaces;


import com.fauna.mapping.MappingContext;
import com.fauna.serialization.FaunaParser;
import java.io.IOException;

public interface IDeserializer<T> {

    T deserialize(MappingContext context, FaunaParser parser) throws IOException;

}

