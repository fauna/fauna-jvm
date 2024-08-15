package com.fauna.codec.codecs;

import com.fauna.exception.ClientException;
import com.fauna.serialization.UTF8FaunaGenerator;
import com.fauna.serialization.UTF8FaunaParser;

import java.io.IOException;

public class ByteArrayCodec extends BaseCodec<byte[]> {

    public static final ByteArrayCodec singleton = new ByteArrayCodec();

    @Override
    public byte[] decode(UTF8FaunaParser parser) throws IOException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case BYTES:
                return parser.getValueAsByteArray();
            default:
                throw new ClientException(this.unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
        }
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, byte[] obj) throws IOException {
        if (obj == null) {
            gen.writeNullValue();
            return;
        }

        gen.writeBytesValue(obj);
    }

    @Override
    public Class<?> getCodecClass() {
        return byte[].class;
    }
}
