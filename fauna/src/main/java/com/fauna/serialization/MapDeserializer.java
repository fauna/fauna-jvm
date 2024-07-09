package com.fauna.serialization;

import com.fauna.common.enums.FaunaTokenType;
import com.fauna.exception.ClientException;
import com.fauna.interfaces.IDeserializer;
import com.fauna.mapping.MappingContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MapDeserializer<T> extends BaseDeserializer<Map<String, T>> {

    private IDeserializer<T> _elemDeserializer;

    public MapDeserializer(IDeserializer<T> elemDeserializer) {
        _elemDeserializer = elemDeserializer;
    }

    @Override
    public Map<String, T> doDeserialize(MappingContext context, UTF8FaunaParser reader)
        throws IOException {
        if (reader.getCurrentTokenType() != FaunaTokenType.START_OBJECT) {
            throw new ClientException(
                "Unexpected token while deserializing into Map: " + reader.getCurrentTokenType());
        }

        Map<String, T> map = new HashMap<>();

        while (reader.read() && reader.getCurrentTokenType() != FaunaTokenType.END_OBJECT) {
            if (reader.getCurrentTokenType() != FaunaTokenType.FIELD_NAME) {
                throw new ClientException(
                    "Unexpected token while deserializing field of Map: "
                        + reader.getCurrentTokenType());
            }

            String fieldName = reader.getValueAsString();
            reader.read();
            T value = _elemDeserializer.deserialize(context, reader);
            map.put(fieldName, value);
        }

        return map;
    }
}
