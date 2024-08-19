package com.fauna.serialization;


import com.fauna.codec.UTF8FaunaParser;
import com.fauna.enums.FaunaTokenType;
import com.fauna.types.Page;
import com.fauna.exception.ClientException;
import com.fauna.interfaces.IDeserializer;

import java.io.IOException;
import java.util.List;

public class PageDeserializer<T> extends BaseDeserializer<Page<T>> {

    private IDeserializer<T> elemDeserializer;
    private IDeserializer<List<T>> dataDeserializer;

    public PageDeserializer(IDeserializer<T> elemDeserializer) {
        this.elemDeserializer = elemDeserializer;
        this.dataDeserializer = new ListDeserializer<>(elemDeserializer);
    }

    private Page<T> deserializePage(UTF8FaunaParser reader, FaunaTokenType endToken) throws IOException {
        List<T> data = null;
        String after = null;


        while (reader.read() && reader.getCurrentTokenType() != endToken) {
            String fieldName = reader.getValueAsString();
            reader.read();

            switch (fieldName) {
                case "data":
                    data = dataDeserializer.deserialize(reader);
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

    public Page<T> wrapDocumentInPage(UTF8FaunaParser reader) throws IOException {
        T elem = this.elemDeserializer.deserialize(reader);
        return new Page<>(List.of(elem), null);
    }

    @Override
    public Page<T> doDeserialize(UTF8FaunaParser reader) throws IOException {
        switch (reader.getCurrentTokenType()) {
            case START_PAGE:
                return deserializePage(reader, FaunaTokenType.END_PAGE);
            case START_OBJECT:
                return deserializePage(reader, FaunaTokenType.END_OBJECT);
            case START_DOCUMENT:
                return wrapDocumentInPage(reader);
            default:
                throw new ClientException(
                    "Unexpected token while deserializing into " + Page.class + ": "
                        + reader.getCurrentTokenType());
        }
    }
}