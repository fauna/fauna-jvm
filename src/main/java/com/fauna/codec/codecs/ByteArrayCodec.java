package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;

/**
 * Codec for encoding and decoding byte arrays to and from Fauna's wire format.
 */
public final class ByteArrayCodec extends BaseCodec<byte[]> {

    public static final ByteArrayCodec SINGLETON = new ByteArrayCodec();

    /**
     * Decodes a byte array from the parser.
     *
     * @param parser the parser to read from
     * @return the decoded byte array, or null if the token represents a null value
     * @throws CodecException if decoding fails due to an unexpected type
     */
    @Override
    public byte[] decode(final UTF8FaunaParser parser) throws CodecException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case BYTES:
                return parser.getValueAsByteArray();
            default:
                throw new CodecException(this.unsupportedTypeDecodingMessage(
                        parser.getCurrentTokenType().getFaunaType(),
                        getSupportedTypes()));
        }
    }

    /**
     * Encodes a byte array to the generator.
     *
     * @param gen the generator to write to
     * @param obj the byte array to encode
     * @throws CodecException if encoding fails
     */
    @Override
    public void encode(final UTF8FaunaGenerator gen, final byte[] obj)
            throws CodecException {
        if (obj == null) {
            gen.writeNullValue();
            return;
        }

        gen.writeBytesValue(obj);
    }

    /**
     * Returns the class type this codec supports.
     *
     * @return byte array class
     */
    @Override
    public Class<?> getCodecClass() {
        return byte[].class;
    }

    /**
     * Returns the Fauna types this codec supports.
     *
     * @return supported Fauna types
     */
    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[] {FaunaType.Bytes, FaunaType.Null};
    }
}
