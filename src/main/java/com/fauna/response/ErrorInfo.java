package com.fauna.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class ErrorInfo {

    private final String code;
    private final String message;
    private final ConstraintFailure[] constraintFailures;
    private final Object abort;

    @JsonCreator
    public ErrorInfo(
        @JsonProperty("code") String code,
        @JsonProperty("message") String message,
        @JsonProperty("constraint_failures") ConstraintFailure[] constraintFailures,
        @JsonProperty("abort") Object abort) {
        this.code = code;
        this.message = message;
        this.constraintFailures = constraintFailures;
        this.abort = abort;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public ConstraintFailure[] getConstraintFailures() {
        return constraintFailures;
    }

    public Optional<Object> getAbort() {
        return Optional.ofNullable(this.abort);
    }

}
