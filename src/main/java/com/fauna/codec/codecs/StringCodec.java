package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;

/**
 * Codec for encoding and decoding {@link String} values.
 */
public final class StringCodec extends BaseCodec<String> {

    public static final StringCodec SINGLETON = new StringCodec();

    @Override
    public String decode(final UTF8FaunaParser parser) throws CodecException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case STRING:
                return parser.getValueAsString();
            case BYTES:
                return parser.getTaggedValueAsString();
            default:
                throw new CodecException(this.unsupportedTypeDecodingMessage(
                        parser.getCurrentTokenType().getFaunaType(),
                        getSupportedTypes()));
        }
    }

    @Override
    public void encode(final UTF8FaunaGenerator gen, final String obj)
            throws CodecException {
        if (obj == null) {
            gen.writeNullValue();
        } else {
            gen.writeStringValue(obj);
        }
    }

    @Override
    public Class<String> getCodecClass() {
        return String.class;
    }

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[] {FaunaType.Bytes, FaunaType.Null,
                FaunaType.String};
    }
}
