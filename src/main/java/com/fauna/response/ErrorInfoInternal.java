package com.fauna.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fauna.codec.json.PassThroughDeserializer;

public class ErrorInfoInternal {

    @JsonProperty("code")
    private String code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("constraint_failures")
    private ConstraintFailure[] constraintFailures;

    @JsonProperty("abort")
    @JsonDeserialize(using = PassThroughDeserializer.class)
    private String abortRaw;


    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public ConstraintFailure[] getConstraintFailures() {
        return constraintFailures;
    }

    public String getAbortRaw() {
        return this.abortRaw;
    }
}
