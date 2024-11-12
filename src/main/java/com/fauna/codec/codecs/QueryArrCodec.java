package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;
import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;
import com.fauna.query.builder.QueryArr;

/**
 * Codec for encoding and decoding {@link QueryArr} objects.
 */
@SuppressWarnings("rawtypes")
public final class QueryArrCodec extends BaseCodec<QueryArr> {

    private final CodecProvider provider;

    /**
     * Creates a new instance of the {@link QueryArrCodec}.
     *
     * @param provider The codec provider used to retrieve codecs for object types.
     */
    public QueryArrCodec(final CodecProvider provider) {
        this.provider = provider;
    }

    @Override
    public QueryArr decode(final UTF8FaunaParser parser) throws CodecException {
        throw new CodecException(
                "Decoding into a QueryFragment is not supported");
    }

    @Override
    public void encode(final UTF8FaunaGenerator gen, final QueryArr obj)
            throws CodecException {
        if (obj == null) {
            gen.writeNullValue();
        } else {
            gen.writeStartObject();
            gen.writeFieldName("array");
            Object unwrapped = obj.get();
            Codec codec = provider.get(unwrapped.getClass());
            //noinspection unchecked
            codec.encode(gen, unwrapped);
            gen.writeEndObject();
        }
    }

    @Override
    public Class<QueryArr> getCodecClass() {
        return QueryArr.class;
    }

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[0];
    }
}
