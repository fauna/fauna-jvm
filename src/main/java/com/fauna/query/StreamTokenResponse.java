package com.fauna.query;

import com.fauna.annotation.FaunaField;

public class StreamTokenResponse {
    @FaunaField(name = "@stream")
    private String token;

    public StreamTokenResponse(String token) {
        this.token = token;
    }

    public StreamTokenResponse() {}

    public String getToken() {
        return this.token;
    }
}
