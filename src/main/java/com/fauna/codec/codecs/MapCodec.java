package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.enums.FaunaTokenType;
import com.fauna.exception.ClientException;
import com.fauna.serialization.UTF8FaunaGenerator;
import com.fauna.serialization.UTF8FaunaParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapCodec<V,L extends Map<String,V>> extends BaseCodec<L> {

    private final Codec<V> valueCodec;

    public MapCodec(Codec<V> valueCodec) {
        this.valueCodec = valueCodec;
    }

    @Override
    public L decode(UTF8FaunaParser parser) throws IOException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case START_OBJECT:
                Map<String, V> map = new HashMap<>();

                while (parser.read() && parser.getCurrentTokenType() != FaunaTokenType.END_OBJECT) {
                    if (parser.getCurrentTokenType() != FaunaTokenType.FIELD_NAME) {
                        throw new ClientException(unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
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
                throw new ClientException(this.unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
        }
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, L obj) throws IOException {
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
        return List.class;
    }
}
