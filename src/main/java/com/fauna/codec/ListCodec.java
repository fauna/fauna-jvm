package com.fauna.codec;

import com.fauna.enums.FaunaTokenType;
import com.fauna.exception.ClientException;
import com.fauna.mapping.MappingContext;
import com.fauna.serialization.UTF8FaunaGenerator;
import com.fauna.serialization.UTF8FaunaParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListCodec<E> implements Codec<List<E>> {

    private final Codec<E> elemCodec;
    private final Class<E> elemType;

    public ListCodec(Class<E> elementType) {
        elemType = elementType;
        elemCodec = CodecProvider.generate(new MappingContext(), elementType);
    }

    @Override
    public List<E> decode(UTF8FaunaParser parser) throws IOException {
        if (parser.getCurrentTokenType() != FaunaTokenType.START_ARRAY) {
            throw new ClientException(
                    "Unexpected token while deserializing into List: " + parser.getCurrentTokenType());
        }

        List<E> list = new ArrayList<>();

        while (parser.read() && parser.getCurrentTokenType() != FaunaTokenType.END_ARRAY) {
            E value = elemCodec.decode(parser);
            list.add(value);
        }

        return list;
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, List<E> obj) throws IOException {
        gen.writeStartArray();
        for (E item : obj) {
            elemCodec.encode(gen, item);
        }
        gen.writeEndArray();
    }

    @Override
    public Class<List<E>> getCodecClass() {
        return null;
    }
}
