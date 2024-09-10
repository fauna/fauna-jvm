package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.exception.CodecException;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;

public class BoolCodec extends BaseCodec<Boolean> {

    public static final BoolCodec singleton = new BoolCodec();

    @Override
    public Boolean decode(UTF8FaunaParser parser) throws CodecException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case TRUE:
            case FALSE:
                return parser.getValueAsBoolean();
            default:
                throw new CodecException(this.unsupportedTypeDecodingMessage(parser.getCurrentTokenType().getFaunaType(), getSupportedTypes()));
        }
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, Boolean obj) throws CodecException {
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
        return new FaunaType[]{FaunaType.Boolean, FaunaType.Null};
    }
}
