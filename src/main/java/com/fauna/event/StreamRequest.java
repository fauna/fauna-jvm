package com.fauna.event;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fauna.client.RequestBuilder;
import jdk.jfr.Event;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

/**
 * This class defines the request body expected by the fauna /stream endpoint.
 */
public class StreamRequest {
    private final EventSource source;
    private final StreamOptions options;

    public StreamRequest(EventSource eventSource, StreamOptions streamOptions) {
        this.source = eventSource;
        this.options = streamOptions;
        if (source == null) {
            throw new IllegalArgumentException("Event source cannot be null.");
        }
        if (options == null) {
            throw new IllegalArgumentException("Stream options cannot be null.");
        }
    }

    public String serialize() throws IOException {
        // Use JsonGenerator directly rather than UTF8FaunaGenerator because this is not FQL. For example,
        // start_ts is a JSON numeric/integer, not a tagged '@long'.
        ByteArrayOutputStream requestBytes = new ByteArrayOutputStream();
        JsonGenerator gen = new JsonFactory().createGenerator(requestBytes);
        gen.writeStartObject();
        gen.writeStringField(RequestBuilder.FieldNames.TOKEN, source.getToken());
        // Only one of cursor / start_ts can be present, prefer cursor.
        // Cannot use ifPresent(val -> ...) because gen.write methods can throw an IOException.
        if (options.getCursor().isPresent()) {
            gen.writeStringField(RequestBuilder.FieldNames.CURSOR, options.getCursor().get());
        } else if (options.getStartTimestamp().isPresent()) {
            gen.writeNumberField(RequestBuilder.FieldNames.START_TS, options.getStartTimestamp().get());
        }
        gen.writeEndObject();
        gen.flush();
        return requestBytes.toString(StandardCharsets.UTF_8);
    }

}
