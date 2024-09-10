package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;

public class EnumCodec<T> extends BaseCodec<T> {
    private final Class<T> enumType;

    public EnumCodec(Class<T> enumType) {
        this.enumType = enumType;
    }

    @Override
    public T decode(UTF8FaunaParser parser) throws CodecException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case STRING:
                //noinspection unchecked
                return (T) Enum.valueOf((Class<Enum>) enumType, parser.getValueAsString());
            default:
                throw new CodecException(this.unsupportedTypeDecodingMessage(parser.getCurrentTokenType().getFaunaType(), getSupportedTypes()));
        }
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, T obj) throws CodecException {
        if (obj == null) {
            gen.writeNullValue();
        } else {
            gen.writeStringValue(((Enum) obj).name());
        }
    }

    @Override
    public Class<?> getCodecClass() {
        return enumType;
    }

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[]{FaunaType.Null, FaunaType.String};
    }
}
