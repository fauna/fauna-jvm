package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;
import com.fauna.query.builder.QueryLiteral;

public class QueryLiteralCodec extends BaseCodec<QueryLiteral> {

    @Override
    public QueryLiteral decode(UTF8FaunaParser parser) throws CodecException {
        throw new CodecException(
                "Decoding into a QueryFragment is not supported");
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, QueryLiteral obj)
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
