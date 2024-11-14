package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;

/**
 * Codec for encoding and decoding {@code Character} values in Fauna's tagged data format.
 */
public final class CharCodec extends BaseCodec<Character> {

    public static final CharCodec SINGLETON = new CharCodec();

    /**
     * Decodes a {@code Character} from the parser.
     *
     * @param parser the parser to read from
     * @return the decoded {@code Character} value, or null if the token represents a null value
     * @throws CodecException if decoding fails due to an unexpected type
     */
    @Override
    public Character decode(final UTF8FaunaParser parser) throws CodecException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case INT:
                return parser.getValueAsCharacter();
            default:
                throw new CodecException(this.unsupportedTypeDecodingMessage(
                        parser.getCurrentTokenType().getFaunaType(),
                        getSupportedTypes()));
        }
    }

    /**
     * Encodes a {@code Character} value to the generator.
     *
     * @param gen the generator to write to
     * @param obj the {@code Character} value to encode
     * @throws CodecException if encoding fails
     */
    @Override
    public void encode(final UTF8FaunaGenerator gen, final Character obj)
            throws CodecException {
        if (obj == null) {
            gen.writeNullValue();
        } else {
            gen.writeCharValue(obj);
        }
    }

    /**
     * Returns the class type this codec supports.
     *
     * @return {@code Character} class
     */
    @Override
    public Class<Character> getCodecClass() {
        return Character.class;
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
