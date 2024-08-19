package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.enums.FaunaTokenType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;

import java.io.IOException;
import java.util.Optional;

public class OptionalCodec<E,L extends Optional<E>> extends BaseCodec<L> {

    private final Codec<E> valueCodec;

    public OptionalCodec(Codec<E> valueCodec) {
        this.valueCodec = valueCodec;
    }

    @Override
    public L decode(UTF8FaunaParser parser) throws IOException {
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
    public void encode(UTF8FaunaGenerator gen, L obj) throws IOException {
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
}
