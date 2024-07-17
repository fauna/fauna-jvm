package com.fauna.interfaces;


import com.fauna.serialization.UTF8FaunaParser;
import java.io.IOException;

public interface IDeserializer<T> {

    T deserialize(UTF8FaunaParser parser) throws IOException;

}

