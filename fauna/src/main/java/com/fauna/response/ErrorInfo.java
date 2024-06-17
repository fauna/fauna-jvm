package com.fauna.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorInfo {

    private String code;
    private String message;
    private Object constraintFailures;
    private Object abort;

    @JsonCreator
    public ErrorInfo(
        @JsonProperty("code") String code,
        @JsonProperty("message") String message,
        @JsonProperty("constraint_failures") Object constraintFailures,
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

    public Object getConstraintFailures() {
        return constraintFailures;
    }

    public Object getAbort() {
        return abort;
    }

}
