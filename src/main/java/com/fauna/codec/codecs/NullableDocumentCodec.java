package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.FaunaTokenType;
import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;
import com.fauna.exception.NullDocumentException;
import com.fauna.types.NonNullDocument;
import com.fauna.types.NullDocument;
import com.fauna.types.NullableDocument;

/**
 * Codec for encoding and decoding NullableDocument types.
 *
 * @param <E> The type of the value in the document.
 * @param <L> The type of the document (NullableDocument).
 */
public final class NullableDocumentCodec<E, L extends NullableDocument<E>>
        extends BaseCodec<L> {

    private final Codec<E> valueCodec;

    /**
     * Constructs a {@code NullableDocumentCodec} with the specified {@code Codec}.
     *
     * @param valueCodec The codec to use for the value.
     */
    public NullableDocumentCodec(final Codec<E> valueCodec) {
        this.valueCodec = valueCodec;
    }

    @Override
    @SuppressWarnings("unchecked")
    public L decode(final UTF8FaunaParser parser) throws CodecException {
        if (parser.getCurrentTokenType() == FaunaTokenType.NULL) {
            return null;
        }

        try {
            E decoded = valueCodec.decode(parser);

            if (decoded instanceof NullDocument) {
                return (L) decoded;
            }

            return (L) new NonNullDocument<>(decoded);
        } catch (NullDocumentException e) {
            return (L) new NullDocument<>(e.getId(), e.getCollection(),
                    e.getNullCause());
        }
    }

    @Override
    public void encode(final UTF8FaunaGenerator gen, final L obj) throws CodecException {
        if (obj instanceof NonNullDocument) {
            @SuppressWarnings("unchecked")
            NonNullDocument<E> nn = (NonNullDocument<E>) obj;
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
