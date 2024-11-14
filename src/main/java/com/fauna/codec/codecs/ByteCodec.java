package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;

/**
 * Codec for encoding and decoding {@code Byte} values in Fauna's tagged data format.
 */
public final class ByteCodec extends BaseCodec<Byte> {

    public static final ByteCodec SINGLETON = new ByteCodec();

    /**
     * Decodes a {@code Byte} from the parser.
     *
     * @param parser the parser to read from
     * @return the decoded {@code Byte} value, or null if the token represents a null value
     * @throws CodecException if decoding fails due to an unexpected type
     */
    @Override
    public Byte decode(final UTF8FaunaParser parser) throws CodecException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case INT:
                return parser.getValueAsByte();
            default:
                throw new CodecException(this.unsupportedTypeDecodingMessage(
                        parser.getCurrentTokenType().getFaunaType(),
                        getSupportedTypes()));
        }
    }

    /**
     * Encodes a {@code Byte} value to the generator.
     *
     * @param gen the generator to write to
     * @param obj the {@code Byte} value to encode
     * @throws CodecException if encoding fails
     */
    @Override
    public void encode(final UTF8FaunaGenerator gen, final Byte obj) throws CodecException {
        if (obj == null) {
            gen.writeNullValue();
        } else {
            gen.writeIntValue(obj);
        }
    }

    /**
     * Returns the class type this codec supports.
     *
     * @return {@code Byte} class
     */
    @Override
    public Class<Byte> getCodecClass() {
        return Byte.class;
    }

    /**
     * Returns the Fauna types this codec supports.
     *
     * @return supported Fauna types
     */
    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[] {FaunaType.Int, FaunaType.Null};
    }
}
