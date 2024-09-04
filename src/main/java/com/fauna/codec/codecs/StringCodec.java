package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.exception.ClientException;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;

import java.io.IOException;

public class StringCodec extends BaseCodec<String> {

    public static final StringCodec singleton = new StringCodec();

    @Override
    public String decode(UTF8FaunaParser parser) throws IOException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case STRING:
                return parser.getValueAsString();
            case BYTES:
                return parser.getTaggedValueAsString();
            default:
                throw new ClientException(this.unsupportedTypeDecodingMessage(parser.getCurrentTokenType().getFaunaType(), getSupportedTypes()));
        }
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, String obj) throws IOException {
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
        return new FaunaType[]{FaunaType.Null, FaunaType.String, FaunaType.Bytes };
    }
}
