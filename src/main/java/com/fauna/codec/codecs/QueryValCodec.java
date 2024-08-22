package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.ClientException;
import com.fauna.query.builder.QueryArr;
import com.fauna.query.builder.QueryVal;

import java.io.IOException;

public class QueryValCodec extends BaseCodec<QueryVal> {

    private final CodecProvider provider;

    public QueryValCodec(CodecProvider provider) {

        this.provider = provider;
    }

    @Override
    public QueryVal decode(UTF8FaunaParser parser) throws IOException {
        throw new ClientException("Decoding into a QueryFragment is not supported");
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, QueryVal obj) throws IOException {
        if (obj == null) {
            gen.writeNullValue();
        } else {
            gen.writeStartObject();
            gen.writeFieldName("value");
            Object unwrapped = obj.get();
            Codec codec = provider.get(unwrapped.getClass());
            codec.encode(gen, unwrapped);
            gen.writeEndObject();
        }
    }

    @Override
    public Class<QueryVal> getCodecClass() {
        return QueryVal.class;
    }
}
