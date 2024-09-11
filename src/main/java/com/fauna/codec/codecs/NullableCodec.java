package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.FaunaTokenType;
import com.fauna.codec.FaunaType;
import com.fauna.exception.CodecException;
import com.fauna.exception.NullDocumentException;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.types.NonNull;
import com.fauna.types.NullDoc;
import com.fauna.types.Nullable;

public class NullableCodec<E,L extends Nullable<E>> extends BaseCodec<L> {

    private final Codec<E> valueCodec;

    public NullableCodec(Codec<E> valueCodec) {
        this.valueCodec = valueCodec;
    }

    @Override
    @SuppressWarnings("unchecked")
    public L decode(UTF8FaunaParser parser) throws CodecException {
        if (parser.getCurrentTokenType() == FaunaTokenType.NULL) {
            return null;
        }

        try {
            E decoded = valueCodec.decode(parser);

            if (decoded instanceof NullDoc) return (L) decoded;

            return (L) new NonNull<>(decoded);
        } catch (NullDocumentException e) {
            return (L) new NullDoc<>(e.getId(), e.getCollection(), e.getNullCause());
        }
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, L obj) throws CodecException {
        if (obj instanceof NonNull) {
            @SuppressWarnings("unchecked")
            NonNull<E> nn = (NonNull<E>) obj;
            valueCodec.encode(gen, nn.get());
        } else {
            throw new CodecException(unsupportedTypeMessage(obj.getClass()));
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
