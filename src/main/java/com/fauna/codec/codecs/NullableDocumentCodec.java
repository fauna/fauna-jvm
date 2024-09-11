package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.FaunaTokenType;
import com.fauna.codec.FaunaType;
import com.fauna.exception.ClientException;
import com.fauna.exception.NullDocumentException;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.types.NonNullDocument;
import com.fauna.types.NullDocument;
import com.fauna.types.NullableDocument;

import java.io.IOException;

public class NullableDocumentCodec<E,L extends NullableDocument<E>> extends BaseCodec<L> {

    private final Codec<E> valueCodec;

    public NullableDocumentCodec(Codec<E> valueCodec) {
        this.valueCodec = valueCodec;
    }

    @Override
    @SuppressWarnings("unchecked")
    public L decode(UTF8FaunaParser parser) throws IOException {
        if (parser.getCurrentTokenType() == FaunaTokenType.NULL) {
            return null;
        }

        try {
            E decoded = valueCodec.decode(parser);

            if (decoded instanceof NullDocument) return (L) decoded;

            return (L) new NonNullDocument<>(decoded);
        } catch (NullDocumentException e) {
            return (L) new NullDocument<>(e.getId(), e.getCollection(), e.getNullCause());
        }
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, L obj) throws IOException {
        if (obj instanceof NonNullDocument) {
            @SuppressWarnings("unchecked")
            NonNullDocument<E> nn = (NonNullDocument<E>) obj;
            valueCodec.encode(gen, nn.get());
        } else {
            throw new ClientException(unsupportedTypeMessage(obj.getClass()));
        }
    }

    @Override
    public Class<?> getCodecClass() {
        return valueCodec.getCodecClass();
    }

    @Override
    public FaunaType[] getSupportedTypes() {
        return valueCodec.getSupportedTypes();
    }
}
