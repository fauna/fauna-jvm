package com.fauna.perf.testdata;

import com.fasterxml.jackson.annotation.JsonProperty;

// Define TestResponse
public class TestResponse {
    @JsonProperty("typed")
    private boolean typed = false;

    @JsonProperty("page")
    private boolean page = false;

    // Getters and setters
    public boolean isTyped() {
        return typed;
    }

    public void setTyped(boolean typed) {
        this.typed = typed;
    }

    public boolean isPage() {
        return page;
    }

    public void setPage(boolean page) {
        this.page = page;
    }
}
