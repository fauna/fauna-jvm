package com.fauna.response;

import java.util.List;
import java.util.Optional;

public class ConstraintFailure {
    private final String message;

    private final String name;

    private final List<List<Object>> paths;

    public ConstraintFailure(String message, String name, List<List<Object>> paths) {
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

    public List<List<Object>> getPaths() {
        return paths != null ? paths : List.of();
    }

}
