package com.fauna.event;

import com.fauna.query.builder.Query;

import java.util.Objects;

/**
 * Represents an <a href="https://docs.fauna.com/fauna/current/reference/cdc/#event-source">event source</a>. You can consume event sources
as <a href="https://docs.fauna.com/fauna/current/reference/cdc/#event-feeds">event feeds</a> by
 * calling {@link com.fauna.client.FaunaClient#feed(EventSource, FeedOptions, Class) FaunaClient.feed()}
 * or as <a href="https://docs.fauna.com/fauna/current/reference/cdc/#event-streaming">event streams</a> by calling
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
     * Compares this {@code EventSource} with another object for equality.
     *
     * @param o The object to compare with.
     * @return {@code true} if the specified object is equal to this {@code EventSource}; {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (getClass() != o.getClass()) {
            return false;
        }

        EventSource c = (EventSource) o;

        return Objects.equals(token, c.token);
    }

    /**
     * Returns the hash code for this {@code EventSource}.
     *
     * @return An {@code int} representing the hash code of this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(token);
    }
}
