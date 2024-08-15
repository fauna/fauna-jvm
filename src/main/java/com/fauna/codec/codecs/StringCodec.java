package com.fauna.codec.codecs;

import com.fauna.exception.ClientException;
import com.fauna.serialization.UTF8FaunaGenerator;
import com.fauna.serialization.UTF8FaunaParser;

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
            default:
                throw new ClientException(this.unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
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
}
