package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;

/**
 * Codec for encoding and decoding Java Enum types in the Fauna tagged data format.
 *
 * @param <T> The type of the enum.
 */
public final class EnumCodec<T> extends BaseCodec<T> {
    private final Class<T> enumType;

    /**
     * Constructs an {@code EnumCodec} for the specified enum type.
     *
     * @param enumType The enum class to be encoded and decoded.
     */
    public EnumCodec(final Class<T> enumType) {
        this.enumType = enumType;
    }

    @Override
    public T decode(final UTF8FaunaParser parser) throws CodecException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case STRING:
                //noinspection unchecked,rawtypes
                return (T) Enum.valueOf((Class<Enum>) enumType, parser.getValueAsString());
            default:
                throw new CodecException(this.unsupportedTypeDecodingMessage(
                        parser.getCurrentTokenType().getFaunaType(),
                        getSupportedTypes()));
        }
    }

    @Override
    public void encode(final UTF8FaunaGenerator gen, final T obj) throws CodecException {
        if (obj == null) {
            gen.writeNullValue();
        } else {
            gen.writeStringValue(((Enum<?>) obj).name());
        }
    }

    @Override
    public Class<?> getCodecClass() {
        return enumType;
    }

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[] {FaunaType.Null, FaunaType.String};
    }
}
