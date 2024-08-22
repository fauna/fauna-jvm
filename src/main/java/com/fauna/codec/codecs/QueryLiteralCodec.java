package com.fauna.codec.codecs;

import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.ClientException;
import com.fauna.query.builder.QueryLiteral;

import java.io.IOException;

public class QueryLiteralCodec extends BaseCodec<QueryLiteral> {

    @Override
    public QueryLiteral decode(UTF8FaunaParser parser) throws IOException {
        throw new ClientException("Decoding into a QueryFragment is not supported");
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, QueryLiteral obj) throws IOException {
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
}
