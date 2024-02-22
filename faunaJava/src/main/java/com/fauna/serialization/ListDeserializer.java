package com.fauna.serialization;

import com.fauna.common.enums.FaunaTokenType;
import com.fauna.exception.SerializationException;
import com.fauna.interfaces.IDeserializer;
import com.fauna.mapping.MappingContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListDeserializer<T> extends BaseDeserializer<List<T>> {

    private IDeserializer<T> _elemDeserializer;

    public ListDeserializer(IDeserializer<T> elemDeserializer) {
        _elemDeserializer = elemDeserializer;
    }

    @Override
    public List<T> doDeserialize(MappingContext context, FaunaParser reader)
        throws IOException {
        if (reader.getCurrentTokenType() != FaunaTokenType.START_ARRAY) {
            throw new SerializationException(
                "Unexpected token while deserializing into List: " + reader.getCurrentTokenType());
        }

        List<T> list = new ArrayList<>();

        while (reader.read() && reader.getCurrentTokenType() != FaunaTokenType.END_ARRAY) {
            T value = _elemDeserializer.deserialize(context, reader);
            list.add(value);
        }

        return list;
    }
}