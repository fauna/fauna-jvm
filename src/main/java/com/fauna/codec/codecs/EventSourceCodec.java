package com.fauna.codec.codecs;

import com.fauna.codec.FaunaTokenType;
import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.event.EventSource;
import com.fauna.exception.CodecException;

/**
 * Codec for encoding and decoding {@link EventSource} instances in the Fauna tagged format.
 */
public final class EventSourceCodec extends BaseCodec<EventSource> {

    @Override
    public EventSource decode(final UTF8FaunaParser parser)
            throws CodecException {
        if (parser.getCurrentTokenType() == FaunaTokenType.STREAM) {
            return new EventSource(parser.getTaggedValueAsString());
        } else {
            throw new CodecException(this.unsupportedTypeDecodingMessage(
                    parser.getCurrentTokenType().getFaunaType(),
                    getSupportedTypes()));
        }
    }

    @Override
    public void encode(final UTF8FaunaGenerator gen, final EventSource obj)
            throws CodecException {
        throw new CodecException("Cannot encode EventSource");
    }

    @Override
    public Class<?> getCodecClass() {
        return EventSource.class;
    }

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[] {FaunaType.Stream};
    }
}
