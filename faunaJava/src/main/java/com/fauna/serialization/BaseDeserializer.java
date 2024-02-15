package com.fauna.serialization;

import com.fauna.interfaces.IDeserializer;
import java.io.IOException;

public abstract class BaseDeserializer<T> implements IDeserializer<T> {

    public abstract T deserialize(SerializationContext context, FaunaParser parser)
        throws IOException;
}
