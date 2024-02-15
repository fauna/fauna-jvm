package com.fauna.serialization;

import com.fauna.interfaces.IDeserializer;
import com.fauna.mapping.MappingContext;
import java.io.IOException;

public abstract class BaseDeserializer<T> implements IDeserializer<T> {

    public abstract T deserialize(MappingContext context, FaunaParser parser)
        throws IOException;
}
