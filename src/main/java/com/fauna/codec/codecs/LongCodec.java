package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;

public class LongCodec extends BaseCodec<Long> {

    public static final LongCodec singleton = new LongCodec();

    @Override
    public Long decode(UTF8FaunaParser parser) throws CodecException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case INT:
            case LONG:
                return parser.getValueAsLong();
            default:
                throw new CodecException(this.unsupportedTypeDecodingMessage(
                        parser.getCurrentTokenType().getFaunaType(),
                        getSupportedTypes()));
        }
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, Long obj) throws CodecException {
        if (obj == null) {
            gen.writeNullValue();
        } else {
            gen.writeLongValue(obj);
        }
    }

    @Override
    public Class<Long> getCodecClass() {
        return Long.class;
    }

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[] {FaunaType.Int, FaunaType.Long, FaunaType.Null};
    }
}
