package com.fauna.query;

import java.util.Optional;

public class AfterToken {
    private final String token;

    public AfterToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public static Optional<AfterToken> fromString(String token) {
        return Optional.ofNullable(token != null ? new AfterToken(token) : null);
    }
}
