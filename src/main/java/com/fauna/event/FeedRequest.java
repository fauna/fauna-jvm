package com.fauna.event;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fauna.client.RequestBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FeedRequest {
    private final EventSource source;
    private final FeedOptions options;

    public FeedRequest(EventSource source, FeedOptions options) {
        this.source = source;
        this.options = options;
        if (source == null) {
            throw new IllegalArgumentException("EventSource cannot be null.");
        }
        if (options == null) {
            throw new IllegalArgumentException("FeedOptions cannot be null.");
        }
    }

    public String serialize() throws IOException {
        // Use JsonGenerator directly rather than UTF8FaunaGenerator because this is not FQL. For example,
        // start_ts is a JSON numeric/integer, not a tagged '@long'.
        ByteArrayOutputStream requestBytes = new ByteArrayOutputStream();
        JsonGenerator gen = new JsonFactory().createGenerator(requestBytes);
        gen.writeStartObject();
        gen.writeStringField(RequestBuilder.FieldNames.TOKEN,
                source.getToken());
        // Cannot use ifPresent(val -> ...) because gen.write methods can throw an IOException.
        if (options.getCursor().isPresent()) {
            gen.writeStringField(RequestBuilder.FieldNames.CURSOR,
                    options.getCursor().get());
        }
        if (options.getStartTs().isPresent()) {
            gen.writeNumberField(RequestBuilder.FieldNames.START_TS,
                    options.getStartTs().get());
        }
        if (options.getPageSize().isPresent()) {
            gen.writeNumberField(RequestBuilder.FieldNames.PAGE_SIZE,
                    options.getPageSize().get());
        }
        gen.writeEndObject();
        gen.flush();
        return requestBytes.toString(StandardCharsets.UTF_8);
    }

    public static FeedRequest fromResponse(EventSourceResponse resp,
                                           FeedOptions options) {
        return new FeedRequest(EventSource.fromToken(resp.getToken()), options);
    }

}
