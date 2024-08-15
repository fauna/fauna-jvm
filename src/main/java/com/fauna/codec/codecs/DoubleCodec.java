package com.fauna.codec.codecs;

import com.fauna.exception.ClientException;
import com.fauna.serialization.UTF8FaunaGenerator;
import com.fauna.serialization.UTF8FaunaParser;

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
                throw new ClientException(this.unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
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
}
