package com.fauna.serialization;


import com.fauna.enums.FaunaTokenType;
import com.fauna.types.Page;
import com.fauna.exception.ClientException;
import com.fauna.interfaces.IDeserializer;

import java.io.IOException;
import java.util.List;

public class PageDeserializer<T> extends BaseDeserializer<Page<T>> {

    private IDeserializer<List<T>> _dataDeserializer;

    public PageDeserializer(IDeserializer<T> elemDeserializer) {
        _dataDeserializer = new ListDeserializer<>(elemDeserializer);
    }

    @Override
    public Page<T> doDeserialize(UTF8FaunaParser reader)
        throws IOException {
        FaunaTokenType endToken;
        switch (reader.getCurrentTokenType()) {
            case START_PAGE:
                endToken = FaunaTokenType.END_PAGE;
                break;
            case START_OBJECT:
                endToken = FaunaTokenType.END_OBJECT;
                break;
            default:
                throw new ClientException(
                    "Unexpected token while deserializing into " + Page.class + ": "
                        + reader.getCurrentTokenType());
        }

        List<T> data = null;
        String after = null;

        while (reader.read() && reader.getCurrentTokenType() != endToken) {
            String fieldName = reader.getValueAsString();
            reader.read();

            switch (fieldName) {
                case "data":
                    data = _dataDeserializer.deserialize(reader);
                    break;
                case "after":
                    after = reader.getValueAsString();
                    break;
            }
        }

        if (data == null) {
            throw new ClientException("No page data found while deserializing into Page<T>");
        }

        return new Page<>(data, after);
    }
}