package com.fauna.query;


public class StreamTokenResponse {
    private String token;

    public StreamTokenResponse(String token) {
        this.token = token;
    }

    public StreamTokenResponse() {}

    public String getToken() {
        return this.token;
    }
}
