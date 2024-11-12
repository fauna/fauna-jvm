package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;

/**
 * Codec for encoding and decoding {@link Float} values in the Fauna tagged data format.
 */
public final class FloatCodec extends BaseCodec<Float> {

    public static final FloatCodec SINGLETON = new FloatCodec();

    @Override
    public Float decode(final UTF8FaunaParser parser) throws CodecException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case INT:
            case LONG:
            case DOUBLE:
                return parser.getValueAsFloat();
            default:
                throw new CodecException(this.unsupportedTypeDecodingMessage(
                        parser.getCurrentTokenType().getFaunaType(),
                        getSupportedTypes()));
        }
    }

    @Override
    public void encode(final UTF8FaunaGenerator gen, final Float obj)
            throws CodecException {
        if (obj == null) {
            gen.writeNullValue();
        } else {
            gen.writeDoubleValue(obj);
        }
    }

    @Override
    public Class<Float> getCodecClass() {
        return Float.class;
    }

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[] {FaunaType.Double, FaunaType.Int, FaunaType.Long,
                FaunaType.Null};
    }
}
