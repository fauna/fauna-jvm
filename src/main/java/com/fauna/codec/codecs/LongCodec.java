package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;

/**
 * A codec for encoding and decoding {@link Long} values in Fauna's tagged data format.
 */
public final class LongCodec extends BaseCodec<Long> {

    public static final LongCodec SINGLETON = new LongCodec();

    @Override
    public Long decode(final UTF8FaunaParser parser) throws CodecException {
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
    public void encode(final UTF8FaunaGenerator gen, final Long obj) throws CodecException {
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
