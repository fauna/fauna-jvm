package com.fauna.response.wire;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fauna.codec.json.PassThroughDeserializer;
import com.fauna.response.ConstraintFailure;

import java.util.Optional;

public class ErrorInfoWire {

    @JsonProperty("code")
    private String code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("constraint_failures")
    private ConstraintFailureWire[] constraintFailures;

    @JsonProperty("abort")
    @JsonDeserialize(using = PassThroughDeserializer.class)
    private String abort;


    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Optional<ConstraintFailureWire[]> getConstraintFailures() {
        return Optional.ofNullable(constraintFailures);
    }

    public Optional<String> getAbort() {
        return Optional.ofNullable(this.abort);
    }
}
