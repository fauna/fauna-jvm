package com.fauna.codec.codecs;

import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.enums.FaunaTokenType;
import com.fauna.exception.ClientException;
import com.fauna.query.StreamTokenResponse;

import java.io.IOException;

public class StreamTokenResponseCodec extends BaseCodec<StreamTokenResponse> {

    @Override
    public StreamTokenResponse decode(UTF8FaunaParser parser) throws IOException {
        if (parser.getCurrentTokenType() == FaunaTokenType.STREAM) {
            return new StreamTokenResponse(parser.getTaggedValueAsString());
        } else {
            throw new ClientException("Unexpected token type for stream token: " + parser.getCurrentTokenType());
        }
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, StreamTokenResponse obj) throws IOException {
        throw new ClientException("Cannot encode StreamTokenResponse");

    }

    @Override
    public Class<?> getCodecClass() {
        return StreamTokenResponse.class;
    }
}
