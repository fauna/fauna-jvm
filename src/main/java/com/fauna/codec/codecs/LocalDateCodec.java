package com.fauna.codec.codecs;

import com.fauna.exception.ClientException;
import com.fauna.serialization.UTF8FaunaGenerator;
import com.fauna.serialization.UTF8FaunaParser;

import java.io.IOException;
import java.time.LocalDate;

public class LocalDateCodec extends BaseCodec<LocalDate> {

    public static final LocalDateCodec SINGLETON = new LocalDateCodec();

    @Override
    public LocalDate decode(UTF8FaunaParser parser) throws IOException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case DATE:
                return parser.getValueAsLocalDate();
            default:
                throw new ClientException(this.unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
        }
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, LocalDate obj) throws IOException {
        if (obj == null) {
            gen.writeNullValue();
        } else {
            gen.writeDateValue(obj);
        }
    }

    @Override
    public Class<LocalDate> getCodecClass() {
        return LocalDate.class;
    }
}
