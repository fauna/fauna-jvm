package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.FaunaTokenType;
import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;

import java.util.Optional;

/**
 * Codec for encoding and decoding Optional types.
 *
 * @param <E> The type of the value inside the Optional.
 * @param <L> The type of the Optional (Optional<E>).
 */
public final class OptionalCodec<E, L extends Optional<E>> extends BaseCodec<L> {

    private final Codec<E> valueCodec;

    /**
     * Constructs a {@code OptionalCodec} with the specified {@code Codec}.
     *
     * @param valueCodec The codec to use for the value.
     */
    public OptionalCodec(final Codec<E> valueCodec) {
        this.valueCodec = valueCodec;
    }

    @Override
    public L decode(final UTF8FaunaParser parser) throws CodecException {
        if (parser.getCurrentTokenType() == FaunaTokenType.NULL) {
            @SuppressWarnings("unchecked")
            L res = (L) Optional.empty();
            return res;
        }

        @SuppressWarnings("unchecked")
        L res = (L) Optional.of(valueCodec.decode(parser));
        return res;
    }

    @Override
    public void encode(final UTF8FaunaGenerator gen, final L obj) throws CodecException {
        if (obj == null || obj.isEmpty()) {
            gen.writeNullValue();
            return;
        }

        valueCodec.encode(gen, obj.get());
    }

    @Override
    public Class<?> getCodecClass() {
        return Optional.class;
    }

    @Override
    public FaunaType[] getSupportedTypes() {
        return valueCodec.getSupportedTypes();
    }
}
