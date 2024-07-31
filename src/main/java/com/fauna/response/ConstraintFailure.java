package com.fauna.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class ConstraintFailure {
    private final String message;
    private final String name;
    // TODO: Need to support String or Integer here.
    private final String[][] paths;

    @JsonCreator
    public ConstraintFailure(
            @JsonProperty("message") String message,
            @JsonProperty("name") String name,
            @JsonProperty("paths") String[][] paths) {
        this.message = message;
        this.name = name;
        this.paths = paths;
    }

    public String getMessage() {
        return this.message;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(this.name);
    }

    public Optional<String[][]> getPaths() {
        return Optional.ofNullable(this.paths);
    }

}
