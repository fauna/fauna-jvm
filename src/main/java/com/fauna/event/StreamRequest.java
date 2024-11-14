package com.fauna.event;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fauna.client.RequestBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Defines the request body for interacting with the Fauna /stream endpoint.
 * <p>
 * The {@code StreamRequest} class constructs a JSON request body that includes
 * an {@link EventSource} and {@link StreamOptions} to configure the request parameters.
 */
public class StreamRequest {

    private final EventSource source;
    private final StreamOptions options;

    /**
     * Constructs a {@code StreamRequest} with the specified event source and options.
     *
     * @param eventSource   The {@link EventSource} providing the event source token.
     * @param streamOptions The {@link StreamOptions} specifying additional request options.
     * @throws IllegalArgumentException if {@code eventSource} or {@code streamOptions} is null.
     */
    public StreamRequest(final EventSource eventSource, final StreamOptions streamOptions) {
        if (eventSource == null) {
            throw new IllegalArgumentException("Event source cannot be null.");
        }
        if (streamOptions == null) {
            throw new IllegalArgumentException("Stream options cannot be null.");
        }
        this.source = eventSource;
        this.options = streamOptions;
    }

    /**
     * Serializes this {@code StreamRequest} to a JSON string for the Fauna /stream endpoint.
     *
     * <p>The JSON includes fields based on the {@link EventSource} and {@link StreamOptions}
     * configurations. Either the cursor or start timestamp is included, with cursor taking precedence.
     *
     * @return A JSON-formatted {@code String} representing this stream request.
     * @throws IOException if an error occurs during serialization.
     */
    public String serialize() throws IOException {
        ByteArrayOutputStream requestBytes = new ByteArrayOutputStream();
        JsonGenerator gen = new JsonFactory().createGenerator(requestBytes);

        gen.writeStartObject();
        gen.writeStringField(RequestBuilder.FieldNames.TOKEN, source.getToken());

        // Prefer cursor if present, otherwise use start timestamp.
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
