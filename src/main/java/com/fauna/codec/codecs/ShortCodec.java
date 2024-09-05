package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.exception.ClientException;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;

import java.io.IOException;

public class ShortCodec extends BaseCodec<Short> {

    public static final ShortCodec singleton = new ShortCodec();

    @Override
    public Short decode(UTF8FaunaParser parser) throws IOException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case INT:
                return parser.getValueAsShort();
            default:
                throw new ClientException(this.unsupportedTypeDecodingMessage(parser.getCurrentTokenType().getFaunaType(), getSupportedTypes()));
        }
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, Short obj) throws IOException {
        if (obj == null) {
            gen.writeNullValue();
        } else {
            gen.writeIntValue(obj);
        }
    }

    @Override
    public Class<Short> getCodecClass() {
        return Short.class;
    }


    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[]{FaunaType.Int, FaunaType.Null};
    }
}
