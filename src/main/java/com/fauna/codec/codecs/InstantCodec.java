package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.exception.CodecException;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;

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
                throw new CodecException(this.unsupportedTypeDecodingMessage(parser.getCurrentTokenType().getFaunaType(), getSupportedTypes()));
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

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[]{FaunaType.Null, FaunaType.Time};
    }
}
