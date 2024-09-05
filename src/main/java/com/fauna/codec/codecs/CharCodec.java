package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.exception.ClientException;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;

import java.io.IOException;

public class CharCodec extends BaseCodec<Character> {

    public static final CharCodec singleton = new CharCodec();

    @Override
    public Character decode(UTF8FaunaParser parser) throws IOException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case INT:
                return parser.getValueAsCharacter();
            default:
                throw new ClientException(this.unsupportedTypeDecodingMessage(parser.getCurrentTokenType().getFaunaType(), getSupportedTypes()));
        }
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, Character obj) throws IOException {
        if (obj == null) {
            gen.writeNullValue();
        } else {
            gen.writeCharValue(obj);
        }
    }

    @Override
    public Class<Character> getCodecClass() {
        return Character.class;
    }

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[]{FaunaType.Int, FaunaType.Null};
    }
}
