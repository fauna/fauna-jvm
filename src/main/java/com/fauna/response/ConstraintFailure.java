package com.fauna.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class ConstraintFailure {
    private final String message;
    private final String name;
    private final Object[][] paths;

    @JsonCreator
    public ConstraintFailure(
            @JsonProperty("message") String message,
            @JsonProperty("name") String name,
            @JsonProperty("paths") Object[][] paths) {
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

    /**
     * Each path returned by Fauna for a constraint failure is an array of strings and integers. But since Java
     * doesn't really have a way to support union types, returning Object (the common parent of String and Integer)
     * seems like the simplest solution.
     *
     * @return
     */
    public Optional<Object[][]> getPaths() {
        return Optional.ofNullable(this.paths);
    }

}
