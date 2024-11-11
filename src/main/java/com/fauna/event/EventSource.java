package com.fauna.event;

import com.fauna.query.builder.Query;

/**
 * Represents an <a href="https://docs.fauna.com/fauna/current/reference/cdc/#event-source">event source</a>. You can consume event sources
as <a href="https://docs.fauna.com/fauna/current/reference/cdc/#event-feeds">Event Feeds</a> by
 * calling {@link com.fauna.client.FaunaClient#feed(EventSource, FeedOptions, Class) FaunaClient.feed()}
 * or as <a href="https://docs.fauna.com/fauna/current/reference/cdc/#event-streaming">Event Streams</a> by calling
 * {@link com.fauna.client.FaunaClient#stream(EventSource, StreamOptions, Class) FaunaClient.stream()}.
 * <p>
 * The {@code EventSource} class provides methods for constructing instances from event source tokens and responses
 */
public class EventSource {
    private final String token;

    /**
     * Constructs a new {@code EventSource} with the specified token.
     *
     * @param token A {@code String} representing the event source.
     */
    public EventSource(final String token) {
        this.token = token;
    }

    /**
     * Retrieves the token for the event source.
     *
     * @return A {@code String} representing the token.
     */
    public String getToken() {
        return token;
    }

    /**
     * Creates an {@code EventSource} from the specified token.
     *
     * @param token A {@code String} representing the token for the event source.
     * @return A new {@code EventSource} instance.
     */
    public static EventSource fromToken(final String token) {
        return new EventSource(token);
    }

    /**
     * Creates an {@code EventSource} from an {@code EventSourceResponse}.
     *
     * @param response An {@code EventSourceResponse} containing the token for the event source.
     * @return A new {@code EventSource} instance.
     */
    public static EventSource fromResponse(final EventSourceResponse response) {
        return new EventSource(response.getToken());
    }
}
