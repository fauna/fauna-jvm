package com.fauna.codec.codecs;

import com.fauna.codec.CodecProvider;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.ClientException;
import com.fauna.stream.StreamRequest;

import java.io.IOException;

public class StreamRequestCodec extends BaseCodec<StreamRequest> {
    private static final String TOKEN_FIELD = "token";
    private static final String CURSOR_FIELD = "cursor";
    private static final String START_TS_FIELD = "start_ts";

    private final CodecProvider provider;
    public StreamRequestCodec(CodecProvider provider) {
        this.provider = provider;
    }

    @Override
    public StreamRequest decode(UTF8FaunaParser parser) throws IOException {
        throw new ClientException("Decoding into a QueryFragment is not supported");
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, StreamRequest obj) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName(TOKEN_FIELD);
        gen.writeStringValue(obj.getToken());
        // Only one of cursor / start_ts can be present, prefer cursor.
        if (obj.getCursor().isPresent()) {
            gen.writeString(CURSOR_FIELD, obj.getCursor().get());
        } else if (obj.getStartTs().isPresent()) {
            gen.writeLong(START_TS_FIELD, obj.getStartTs().get());
        }
    }



    @Override
    public Class<StreamRequest> getCodecClass() {
        return StreamRequest.class;
    }
}
