package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.FaunaTokenType;
import com.fauna.codec.FaunaType;
import com.fauna.exception.CodecException;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;

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
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case START_ARRAY:
                List<E> list = new ArrayList<>();

                while (parser.read() && parser.getCurrentTokenType() != FaunaTokenType.END_ARRAY) {
                    E value = elementCodec.decode(parser);
                    list.add(value);
                }
                @SuppressWarnings("unchecked")
                var typed = (L) list;
                return typed;
            default:
                throw new CodecException(this.unsupportedTypeDecodingMessage(parser.getCurrentTokenType().getFaunaType(), getSupportedTypes()));
        }
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
        return elementCodec.getCodecClass();
    }

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[]{FaunaType.Array, FaunaType.Null};
    }
}
