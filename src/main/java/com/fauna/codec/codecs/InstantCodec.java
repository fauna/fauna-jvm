package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;

import java.time.Instant;

/**
 * Codec for encoding and decoding {@link Instant} values in Fauna's wire format.
 */
public final class InstantCodec extends BaseCodec<Instant> {

    public static final InstantCodec SINGLETON = new InstantCodec();

    @Override
    public Instant decode(final UTF8FaunaParser parser) throws CodecException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case TIME:
                return parser.getValueAsTime();
            default:
                throw new CodecException(this.unsupportedTypeDecodingMessage(
                        parser.getCurrentTokenType().getFaunaType(),
                        getSupportedTypes()));
        }
    }

    @Override
    public void encode(final UTF8FaunaGenerator gen, final Instant obj) throws CodecException {
        if (obj == null) {
            gen.writeNullValue();
        } else {
            gen.writeTimeValue(obj);
        }
    }

    @Override
    public Class<Instant> getCodecClass() {
        return Instant.class;
    }

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[] {FaunaType.Null, FaunaType.Time};
    }
}
