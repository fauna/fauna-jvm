package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;

public class IntCodec extends BaseCodec<Integer> {

    public static final IntCodec singleton = new IntCodec();

    @Override
    public Integer decode(UTF8FaunaParser parser) throws CodecException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case INT:
            case LONG:
                return parser.getValueAsInt();
            default:
                throw new CodecException(this.unsupportedTypeDecodingMessage(
                        parser.getCurrentTokenType().getFaunaType(),
                        getSupportedTypes()));
        }
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, Integer obj)
            throws CodecException {
        if (obj == null) {
            gen.writeNullValue();
        } else {
            gen.writeIntValue(obj);
        }
    }

    @Override
    public Class<Integer> getCodecClass() {
        return Integer.class;
    }

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[] {FaunaType.Int, FaunaType.Long, FaunaType.Null};
    }
}
