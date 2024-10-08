package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.codec.FaunaTokenType;
import com.fauna.exception.CodecException;
import com.fauna.query.StreamTokenResponse;

public class StreamTokenResponseCodec extends BaseCodec<StreamTokenResponse> {

    @Override
    public StreamTokenResponse decode(UTF8FaunaParser parser) throws CodecException {
        if (parser.getCurrentTokenType() == FaunaTokenType.STREAM) {
            return new StreamTokenResponse(parser.getTaggedValueAsString());
        } else {
            throw new CodecException(this.unsupportedTypeDecodingMessage(parser.getCurrentTokenType().getFaunaType(), getSupportedTypes()));
        }
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, StreamTokenResponse obj) throws CodecException {
        throw new CodecException("Cannot encode StreamTokenResponse");

    }

    @Override
    public Class<?> getCodecClass() {
        return StreamTokenResponse.class;
    }

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[]{FaunaType.Stream};
    }
}
