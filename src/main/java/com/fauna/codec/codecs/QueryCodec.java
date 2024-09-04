package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;
import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.ClientException;
import com.fauna.query.builder.Query;
import com.fauna.query.builder.QueryFragment;

import java.io.IOException;

public class QueryCodec extends BaseCodec<Query> {

    private final CodecProvider provider;
    public QueryCodec(CodecProvider provider) {
        this.provider = provider;
    }

    @Override
    public Query decode(UTF8FaunaParser parser) throws IOException {
        throw new ClientException("Decoding into a QueryFragment is not supported");
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, Query obj) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("fql");
        gen.writeStartArray();
        for (QueryFragment f : obj.get()) {
            Codec codec = provider.get(f.getClass());
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
