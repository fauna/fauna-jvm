package com.fauna.event;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fauna.client.RequestBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Represents an <a href="https://docs.fauna.com/fauna/current/reference/cdc/#event-feeds">event feed</a> request from Fauna.
 * <p>
 * The {@code FeedRequest} class contains an {@link EventSource} and {@link FeedOptions} to
 * specify the details of the feed request, such as the cursor, start timestamp, and page size.
 */
public class FeedRequest {
    private final EventSource source;
    private final FeedOptions options;

    /**
     * Constructs a {@code FeedRequest} with the specified event source and options.
     *
     * @param source  The {@link EventSource} containing the event source token.
     * @param options The {@link FeedOptions} specifying additional feed request options.
     * @throws IllegalArgumentException if {@code source} or {@code options} is null.
     */
    public FeedRequest(final EventSource source, final FeedOptions options) {
        if (source == null) {
            throw new IllegalArgumentException("EventSource cannot be null.");
        }
        if (options == null) {
            throw new IllegalArgumentException("FeedOptions cannot be null.");
        }
        this.source = source;
        this.options = options;
    }

    /**
     * Serializes this {@code FeedRequest} to a JSON string.
     *
     * @return A {@code String} representation of this feed request in JSON format.
     * @throws IOException if an error occurs during serialization.
     */
    public String serialize() throws IOException {
        ByteArrayOutputStream requestBytes = new ByteArrayOutputStream();
        JsonGenerator gen = new JsonFactory().createGenerator(requestBytes);

        gen.writeStartObject();
        gen.writeStringField(RequestBuilder.FieldNames.TOKEN, source.getToken());

        if (options.getCursor().isPresent()) {
            gen.writeStringField(RequestBuilder.FieldNames.CURSOR, options.getCursor().get());
        }
        if (options.getStartTs().isPresent()) {
            gen.writeNumberField(RequestBuilder.FieldNames.START_TS, options.getStartTs().get());
        }
        if (options.getPageSize().isPresent()) {
            gen.writeNumberField(RequestBuilder.FieldNames.PAGE_SIZE, options.getPageSize().get());
        }

        gen.writeEndObject();
        gen.flush();
        return requestBytes.toString(StandardCharsets.UTF_8);
    }

    /**
     * Creates a new {@code FeedRequest} from an {@link EventSource}.
     *
     * @param resp    The {@link EventSource} containing the event source token.
     * @param options The {@link FeedOptions} specifying additional feed request options.
     * @return A new {@code FeedRequest} instance based on the response and options.
     */
    public static FeedRequest fromResponse(final EventSource resp, final FeedOptions options) {
        return new FeedRequest(resp, options);
    }
}
