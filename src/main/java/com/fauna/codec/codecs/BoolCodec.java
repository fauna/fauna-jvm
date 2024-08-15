package com.fauna.codec.codecs;

import com.fauna.exception.ClientException;
import com.fauna.serialization.UTF8FaunaGenerator;
import com.fauna.serialization.UTF8FaunaParser;

import java.io.IOException;

public class BoolCodec extends BaseCodec<Boolean> {

    public static final BoolCodec singleton = new BoolCodec();

    @Override
    public Boolean decode(UTF8FaunaParser parser) throws IOException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case TRUE:
            case FALSE:
                return parser.getValueAsBoolean();
            default:
                throw new ClientException(this.unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
        }
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, Boolean obj) throws IOException {
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
}
