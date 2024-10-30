package com.fauna.event;

public class EventSource {
    private final String token;

    public EventSource(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public static EventSource fromToken(String token) {
        return new EventSource(token);
    }

    public static EventSource fromResponse(EventSourceResponse response) {
        return new EventSource(response.getToken());
    }
}
