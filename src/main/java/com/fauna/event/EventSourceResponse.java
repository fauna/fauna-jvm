package com.fauna.event;


import java.util.Objects;

public class EventSourceResponse {
    private String token;

    public EventSourceResponse(String token) {
        this.token = token;
    }

    public EventSourceResponse() {
    }

    public String getToken() {
        return this.token;
    }

    @Override
    public boolean equals(Object o) {
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

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }
}
