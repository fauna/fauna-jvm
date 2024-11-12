package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.FaunaTokenType;
import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;

import java.util.ArrayList;
import java.util.List;

/**
 * A codec for encoding and decoding lists of elements in Fauna's tagged data format.
 *
 * @param <E> The type of elements in the list.
 * @param <L> The type of the list that will hold the elements.
 */
public final class ListCodec<E, L extends List<E>> extends BaseCodec<L> {

    private final Codec<E> elementCodec;

    /**
     * Creates a codec for encoding and decoding lists of elements.
     *
     * @param elementCodec The codec used to encode/decode elements of the list.
     */
    public ListCodec(final Codec<E> elementCodec) {
        this.elementCodec = elementCodec;
    }

    @Override
    public L decode(final UTF8FaunaParser parser) throws CodecException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case START_ARRAY:
                List<E> list = new ArrayList<>();

                while (parser.read() && parser.getCurrentTokenType() !=
                        FaunaTokenType.END_ARRAY) {
                    E value = elementCodec.decode(parser);
                    list.add(value);
                }
                @SuppressWarnings("unchecked")
                var typed = (L) list;
                return typed;
            default:
                throw new CodecException(this.unsupportedTypeDecodingMessage(
                        parser.getCurrentTokenType().getFaunaType(),
                        getSupportedTypes()));
        }
    }

    @Override
    public void encode(final UTF8FaunaGenerator gen, final L obj) throws CodecException {
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
        return new FaunaType[] {FaunaType.Array, FaunaType.Null};
    }
}
