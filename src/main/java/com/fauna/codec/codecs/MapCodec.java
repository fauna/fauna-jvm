package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.FaunaTokenType;
import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;

import java.util.HashMap;
import java.util.Map;

/**
 * A codec for encoding and decoding {@link Map} values in Fauna's wire format.
 * <p>
 * This class handles encoding and decoding of maps, where the keys are strings and the values are of a generic
 * type {@code V}.
 * </p>
 *
 * @param <V> The type of the values in the map.
 * @param <L> The type of the map.
 */
public final class MapCodec<V, L extends Map<String, V>> extends BaseCodec<L> {

    private final Codec<V> valueCodec;

    /**
     * Constructs a {@code MapCodec} with the specified {@code Codec}.
     *
     * @param valueCodec The codec to use for the value.
     */
    public MapCodec(final Codec<V> valueCodec) {
        this.valueCodec = valueCodec;
    }

    @Override
    public L decode(final UTF8FaunaParser parser) throws CodecException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case START_OBJECT:
                Map<String, V> map = new HashMap<>();

                while (parser.read() && parser.getCurrentTokenType() !=
                        FaunaTokenType.END_OBJECT) {
                    if (parser.getCurrentTokenType() !=
                            FaunaTokenType.FIELD_NAME) {
                        throw new CodecException(
                                unexpectedTokenExceptionMessage(
                                        parser.getCurrentTokenType()));
                    }

                    String fieldName = parser.getValueAsString();
                    parser.read();
                    V value = valueCodec.decode(parser);
                    map.put(fieldName, value);
                }

                @SuppressWarnings("unchecked")
                L typed = (L) map;
                return typed;
            default:
                throw new CodecException(this.unsupportedTypeDecodingMessage(
                        parser.getCurrentTokenType().getFaunaType(),
                        getSupportedTypes()));
        }
    }

    @Override
    public void encode(final UTF8FaunaGenerator gen, final L obj) throws CodecException {
        if (obj == null) {
            gen.writeNullValue();
            return;
        }

        boolean shouldEscape = obj.keySet().stream().anyMatch(TAGS::contains);
        if (shouldEscape) {
            gen.writeStartEscapedObject();
        } else {
            gen.writeStartObject();
        }

        for (Map.Entry<?, V> entry : obj.entrySet()) {
            gen.writeFieldName(entry.getKey().toString());

            valueCodec.encode(gen, entry.getValue());
        }

        if (shouldEscape) {
            gen.writeEndEscapedObject();
        } else {
            gen.writeEndObject();
        }
    }

    @Override
    public Class<?> getCodecClass() {
        return valueCodec.getCodecClass();
    }

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[] {FaunaType.Null, FaunaType.Object};
    }
}
