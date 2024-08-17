package com.fauna.codec.codecs;

import com.fauna.exception.ClientException;
import com.fauna.serialization.UTF8FaunaGenerator;
import com.fauna.serialization.UTF8FaunaParser;

import java.io.IOException;
import java.time.Instant;

public class InstantCodec extends BaseCodec<Instant> {

    public static final InstantCodec SINGLETON = new InstantCodec();

    @Override
    public Instant decode(UTF8FaunaParser parser) throws IOException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case TIME:
                return parser.getValueAsTime();
            default:
                throw new ClientException(this.unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
        }
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, Instant obj) throws IOException {
        if (obj == null) {
            gen.writeNullValue();
        } else {
            gen.writeTimeValue(obj);
        }
    }

    @Override
    public Class<Instant> getCodecClass() {
        return Instant.class;
    }
}
