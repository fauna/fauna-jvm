package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;

/**
 * Codec for encoding and decoding FQL boolean values.
 */
public final class BoolCodec extends BaseCodec<Boolean> {

    public static final BoolCodec SINGLETON = new BoolCodec();

    @Override
    public Boolean decode(final UTF8FaunaParser parser) throws CodecException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case TRUE:
            case FALSE:
                return parser.getValueAsBoolean();
            default:
                throw new CodecException(this.unsupportedTypeDecodingMessage(
                        parser.getCurrentTokenType().getFaunaType(),
                        getSupportedTypes()));
        }
    }

    @Override
    public void encode(final UTF8FaunaGenerator gen, final Boolean obj)
            throws CodecException {
        if (obj == null) {
            gen.writeNullValue();
        } else {
            gen.writeBooleanValue(obj);
        }
    }

    @Override
    public Class<Boolean> getCodecClass() {
        return Boolean.class;
    }

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[] {FaunaType.Boolean, FaunaType.Null};
    }
}
