package com.fauna.serialization;

import com.fauna.common.enums.FaunaTokenType;
import com.fauna.interfaces.IDeserializer;
import com.fauna.mapping.MappingContext;
import java.io.IOException;

public abstract class BaseDeserializer<T> implements IDeserializer<T> {

    @Override
    public T deserialize(MappingContext context, UTF8FaunaParser parser) throws IOException {
        if (parser.getCurrentTokenType() == FaunaTokenType.NONE) {
            parser.read();
        }
        return doDeserialize(context, parser);
    }

    protected abstract T doDeserialize(MappingContext context, UTF8FaunaParser parser)
        throws IOException;
}
