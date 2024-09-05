package com.fauna.query;


import java.util.Objects;

public class StreamTokenResponse {
    private String token;

    public StreamTokenResponse(String token) {
        this.token = token;
    }

    public StreamTokenResponse() {}

    public String getToken() {
        return this.token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null) return false;

        if (getClass() != o.getClass()) return false;

        StreamTokenResponse c = (StreamTokenResponse) o;

        return Objects.equals(token, c.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }
}
