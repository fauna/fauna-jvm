package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.exception.CodecException;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;

public class FloatCodec extends BaseCodec<Float> {

    public static final FloatCodec singleton = new FloatCodec();

    @Override
    public Float decode(UTF8FaunaParser parser) throws CodecException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case INT:
            case LONG:
            case DOUBLE:
                return parser.getValueAsFloat();
            default:
                throw new CodecException(this.unsupportedTypeDecodingMessage(parser.getCurrentTokenType().getFaunaType(), getSupportedTypes()));
        }
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, Float obj) throws CodecException {
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
        return new FaunaType[]{FaunaType.Double, FaunaType.Int, FaunaType.Long, FaunaType.Null};
    }
}
