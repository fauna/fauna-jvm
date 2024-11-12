package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;
import com.fauna.query.builder.QueryLiteral;

/**
 * Codec for encoding and decoding {@link QueryLiteral} objects.
 */
public final class QueryLiteralCodec extends BaseCodec<QueryLiteral> {

    /**
     * Creates a new instance of the {@link QueryLiteralCodec}.
     */
    public QueryLiteralCodec() {
        // No additional setup required for the QueryLiteralCodec.
    }

    @Override
    public QueryLiteral decode(final UTF8FaunaParser parser) throws CodecException {
        throw new CodecException(
                "Decoding into a QueryFragment is not supported");
    }

    @Override
    public void encode(final UTF8FaunaGenerator gen, final QueryLiteral obj)
            throws CodecException {
        if (obj == null) {
            gen.writeNullValue();
        } else {
            gen.writeStringValue(obj.get());
        }
    }

    @Override
    public Class<QueryLiteral> getCodecClass() {
        return QueryLiteral.class;
    }

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[0];
    }
}
