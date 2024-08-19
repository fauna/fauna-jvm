package com.fauna.interfaces;


import com.fauna.codec.UTF8FaunaParser;
import java.io.IOException;

public interface IDeserializer<T> {

    T deserialize(UTF8FaunaParser parser) throws IOException;

}

