package com.fauna.codec.codecs;

import com.fauna.codec.FaunaTokenType;
import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.event.EventSourceResponse;
import com.fauna.exception.CodecException;

/**
 * Codec for encoding and decoding {@link EventSourceResponse} instances in the Fauna tagged format.
 */
public final class EventSourceResponseCodec extends BaseCodec<EventSourceResponse> {

    @Override
    public EventSourceResponse decode(final UTF8FaunaParser parser)
            throws CodecException {
        if (parser.getCurrentTokenType() == FaunaTokenType.STREAM) {
            return new EventSourceResponse(parser.getTaggedValueAsString());
        } else {
            throw new CodecException(this.unsupportedTypeDecodingMessage(
                    parser.getCurrentTokenType().getFaunaType(),
                    getSupportedTypes()));
        }
    }

    @Override
    public void encode(final UTF8FaunaGenerator gen, final EventSourceResponse obj)
            throws CodecException {
        throw new CodecException("Cannot encode StreamTokenResponse");
    }

    @Override
    public Class<?> getCodecClass() {
        return EventSourceResponse.class;
    }

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[] {FaunaType.Stream};
    }
}
