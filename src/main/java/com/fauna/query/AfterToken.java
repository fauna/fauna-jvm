package com.fauna.query;

import java.util.Optional;

/**
 * Represents an after token, used to signify the possibility for pagination
 * continuation.
 */
public class AfterToken {

    private final String token;

    /**
     * Constructs an {@code AfterToken} with the specified token.
     *
     * @param token the token to be stored in this {@code AfterToken} instance.
     */
    public AfterToken(final String token) {
        this.token = token;
    }

    /**
     * Returns the token stored in this {@code AfterToken} instance.
     *
     * @return the token as a {@code String}.
     */
    public String getToken() {
        return token;
    }

    /**
     * Creates an {@code AfterToken} instance from the specified token string.
     * If the provided token is {@code null}, an empty {@code Optional} is
     * returned.
     *
     * @param token the token string to convert into an {@code AfterToken}.
     * @return an {@code Optional} containing an {@code AfterToken} if the
     * token is non-null, or an empty {@code Optional} if it is null.
     */
    public static Optional<AfterToken> fromString(final String token) {
        return Optional.ofNullable(
                token != null ? new AfterToken(token) : null);
    }
}
