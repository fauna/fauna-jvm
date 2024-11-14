package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;
import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;
import com.fauna.query.builder.QueryVal;

/**
 * Codec for encoding and decoding {@link QueryVal} objects.
 */
@SuppressWarnings("rawtypes")
public final class QueryValCodec extends BaseCodec<QueryVal> {

    private final CodecProvider provider;

    /**
     * Creates a new instance of the {@link QueryValCodec}.
     *
     * @param provider The codec provider to retrieve codecs for the underlying object types.
     */
    public QueryValCodec(final CodecProvider provider) {
        this.provider = provider;
    }

    @Override
    public QueryVal decode(final UTF8FaunaParser parser) throws CodecException {
        throw new CodecException(
                "Decoding into a QueryFragment is not supported");
    }

    @Override
    public void encode(final UTF8FaunaGenerator gen, final QueryVal obj)
            throws CodecException {
        if (obj == null) {
            gen.writeNullValue();
        } else {
            gen.writeStartObject();
            gen.writeFieldName("value");
            Object unwrapped = obj.get();
            Codec codec = provider.get(unwrapped.getClass());
            //noinspection unchecked
            codec.encode(gen, unwrapped);
            gen.writeEndObject();
        }
    }

    @Override
    public Class<QueryVal> getCodecClass() {
        return QueryVal.class;
    }

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[0];
    }
}
