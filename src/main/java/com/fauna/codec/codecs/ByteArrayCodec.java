package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.exception.ClientException;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;

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
                throw new ClientException(this.unsupportedTypeDecodingMessage(parser.getCurrentTokenType().getFaunaType(), getSupportedTypes()));
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

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[]{FaunaType.Bytes, FaunaType.Null};
    }
}
