package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.enums.FaunaTokenType;
import com.fauna.exception.ClientException;
import com.fauna.serialization.UTF8FaunaGenerator;
import com.fauna.serialization.UTF8FaunaParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListCodec<E,L extends List<E>> extends BaseCodec<L> {

    private final Codec<E> elementCodec;

    public ListCodec(Codec<E> elementCodec) {
        this.elementCodec = elementCodec;
    }

    @Override
    public L decode(UTF8FaunaParser parser) throws IOException {
        if (parser.getCurrentTokenType() != FaunaTokenType.START_ARRAY) {
            throw new ClientException(this.unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
        }

        List<E> list = new ArrayList<>();

        while (parser.read() && parser.getCurrentTokenType() != FaunaTokenType.END_ARRAY) {
            E value = elementCodec.decode(parser);
            list.add(value);
        }

        return (L) list;
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, L obj) throws IOException {
        if (obj == null) {
            gen.writeNullValue();
            return;
        }

        gen.writeStartArray();

        for (E elem : obj) {
            elementCodec.encode(gen, elem);
        }
        gen.writeEndArray();
    }

    @Override
    public Class<?> getCodecClass() {
        return List.class;
    }
}
