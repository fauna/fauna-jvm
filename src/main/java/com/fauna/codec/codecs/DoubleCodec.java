package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.exception.ClientException;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;

import java.io.IOException;

public class DoubleCodec extends BaseCodec<Double> {

    public static final DoubleCodec singleton = new DoubleCodec();

    @Override
    public Double decode(UTF8FaunaParser parser) throws IOException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case INT:
            case LONG:
            case DOUBLE:
                return parser.getValueAsDouble();
            default:
                throw new ClientException(this.unsupportedTypeDecodingMessage(parser.getCurrentTokenType().getFaunaType(), getSupportedTypes()));
        }
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, Double obj) throws IOException {
        if (obj == null) {
            gen.writeNullValue();
        } else {
            gen.writeDoubleValue(obj);
        }
    }

    @Override
    public Class<Double> getCodecClass() {
        return Double.class;
    }

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[]{FaunaType.Double, FaunaType.Int, FaunaType.Long, FaunaType.Null};
    }
}
