package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;

/**
 * Codec for encoding and decoding {@link Double} values in Fauna's wire format.
 */
public final class DoubleCodec extends BaseCodec<Double> {

    public static final DoubleCodec SINGLETON = new DoubleCodec();

    /**
     * Decodes a {@code Double} value from the Fauna wire format.
     *
     * @param parser The parser instance for reading Fauna wire format data.
     * @return The decoded {@code Double} value or {@code null} if the token is {@code NULL}.
     * @throws CodecException If the token type is unsupported for decoding a {@code Double}.
     */
    @Override
    public Double decode(final UTF8FaunaParser parser) throws CodecException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case INT:
            case LONG:
            case DOUBLE:
                return parser.getValueAsDouble();
            default:
                throw new CodecException(this.unsupportedTypeDecodingMessage(
                        parser.getCurrentTokenType().getFaunaType(),
                        getSupportedTypes()));
        }
    }

    /**
     * Encodes a {@code Double} value to Fauna's wire format.
     *
     * @param gen The generator used to write Fauna wire format data.
     * @param obj The {@code Double} value to encode, or {@code null} to write a {@code NULL} value.
     * @throws CodecException If encoding fails.
     */
    @Override
    public void encode(final UTF8FaunaGenerator gen, final Double obj)
            throws CodecException {
        if (obj == null) {
            gen.writeNullValue();
        } else {
            gen.writeDoubleValue(obj);
        }
    }

    /**
     * Returns the class of the codec, which is {@code Double}.
     *
     * @return {@code Double.class}.
     */
    @Override
    public Class<Double> getCodecClass() {
        return Double.class;
    }

    /**
     * Returns the Fauna types supported by this codec.
     *
     * @return An array of {@link FaunaType} supported by this codec.
     */
    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[] {FaunaType.Double, FaunaType.Int, FaunaType.Long, FaunaType.Null};
    }
}
