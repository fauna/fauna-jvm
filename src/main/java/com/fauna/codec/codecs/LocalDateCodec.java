package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;

import java.time.LocalDate;

/**
 * A codec for encoding and decoding {@link LocalDate} in Fauna's wire format.
 */
public final class LocalDateCodec extends BaseCodec<LocalDate> {

    public static final LocalDateCodec SINGLETON = new LocalDateCodec();

    @Override
    public LocalDate decode(final UTF8FaunaParser parser) throws CodecException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case DATE:
                return parser.getValueAsLocalDate();
            default:
                throw new CodecException(this.unsupportedTypeDecodingMessage(
                        parser.getCurrentTokenType().getFaunaType(),
                        getSupportedTypes()));
        }
    }

    @Override
    public void encode(final UTF8FaunaGenerator gen, final LocalDate obj)
            throws CodecException {
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

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[] {FaunaType.Date, FaunaType.Null};
    }
}
