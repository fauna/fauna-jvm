package com.fauna.event;

import java.util.Objects;

/**
 * Represents a response containing details about an <a href="https://docs.fauna.com/fauna/current/reference/cdc/#event-source">event source</a>.
 * <p>
 * The {@code EventSourceResponse} class provides access to the event source token
 * and includes methods for equality checks and hash code generation.
 */
public class EventSourceResponse {
    private String token;

    /**
     * Constructs a new {@code EventSourceResponse} with the specified token.
     *
     * @param token A {@code String} representing the token of the event source.
     */
    public EventSourceResponse(final String token) {
        this.token = token;
    }

    /**
     * Constructs an empty {@code EventSourceResponse}.
     */
    public EventSourceResponse() {
    }

    /**
     * Retrieves the token associated with this event source response.
     *
     * @return A {@code String} representing the token.
     */
    public String getToken() {
        return this.token;
    }

    /**
     * Compares this {@code EventSourceResponse} with another object for equality.
     *
     * @param o The object to compare with.
     * @return {@code true} if the specified object is equal to this {@code EventSourceResponse}; {@code false} otherwise.
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

        EventSourceResponse c = (EventSourceResponse) o;

        return Objects.equals(token, c.token);
    }

    /**
     * Returns the hash code for this {@code EventSourceResponse}.
     *
     * @return An {@code int} representing the hash code of this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(token);
    }
}
