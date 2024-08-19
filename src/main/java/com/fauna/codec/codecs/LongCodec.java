package com.fauna.codec.codecs;

import com.fauna.exception.ClientException;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;

import java.io.IOException;

public class LongCodec extends BaseCodec<Long> {

    public static final LongCodec singleton = new LongCodec();

    @Override
    public Long decode(UTF8FaunaParser parser) throws IOException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case INT:
            case LONG:
                return parser.getValueAsLong();
            default:
                throw new ClientException(this.unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
        }
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, Long obj) throws IOException {
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
}
