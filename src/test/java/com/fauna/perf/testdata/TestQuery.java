package com.fauna.perf.testdata;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

// Define TestQuery
public class TestQuery {
    @JsonProperty("name")
    private String name = "";

    @JsonProperty("parts")
    private List<String> parts = new ArrayList<>();

    @JsonProperty("response")
    private TestResponse response;

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getParts() {
        return parts;
    }

    public void setParts(List<String> parts) {
        this.parts = parts;
    }

    public TestResponse getResponse() {
        return response;
    }

    public void setResponse(TestResponse response) {
        this.response = response;
    }
}
