package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.ClientException;
import com.fauna.query.builder.QueryFragment;
import com.fauna.query.builder.QueryObj;

import java.io.IOException;

public class QueryObjCodec extends BaseCodec<QueryObj> {

    private final CodecProvider provider;

    public QueryObjCodec(CodecProvider provider) {

        this.provider = provider;
    }

    @Override
    public QueryObj decode(UTF8FaunaParser parser) throws IOException {
        throw new ClientException("Decoding into a QueryFragment is not supported");
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, QueryObj obj) throws IOException {
        if (obj == null) {
            gen.writeNullValue();
        } else {
            gen.writeStartObject();
            gen.writeFieldName("object");
            Object unwrapped = obj.get();
            Codec codec = provider.get(unwrapped.getClass());
            codec.encode(gen, unwrapped);
            gen.writeEndObject();
        }
    }

    @Override
    public Class<QueryObj> getCodecClass() {
        return QueryObj.class;
    }
}
