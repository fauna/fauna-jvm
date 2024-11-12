package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;
import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;
import com.fauna.query.builder.Query;
import com.fauna.query.builder.QueryFragment;

/**
 * Codec for encoding and decoding {@link Query} objects.
 */
public final class QueryCodec extends BaseCodec<Query> {

    private final CodecProvider provider;

    /**
     * Creates a new instance of the {@link QueryCodec}.
     *
     * @param provider The codec provider used to retrieve codecs for object types.
     */
    public QueryCodec(final CodecProvider provider) {
        this.provider = provider;
    }

    @Override
    public Query decode(final UTF8FaunaParser parser) throws CodecException {
        throw new CodecException(
                "Decoding into a QueryFragment is not supported");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void encode(final UTF8FaunaGenerator gen, final Query obj)
            throws CodecException {
        gen.writeStartObject();
        gen.writeFieldName("fql");
        gen.writeStartArray();
        for (QueryFragment f : obj.get()) {
            Codec codec = provider.get(f.getClass());
            //noinspection unchecked
            codec.encode(gen, f);
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }

    @Override
    public Class<Query> getCodecClass() {
        return Query.class;
    }

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[0];
    }
}
