package com.fauna.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorInfo {

    @JsonProperty("code")
    private String code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("constraintFailures")
    private Object constraintFailures;

    @JsonProperty("abort")
    private Object abort;

    public ErrorInfo(String code, String message, Object constraintFailures, Object abort) {
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
