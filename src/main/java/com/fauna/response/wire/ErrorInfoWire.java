package com.fauna.response.wire;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fauna.codec.json.PassThroughDeserializer;
import com.fauna.response.ConstraintFailure;


import java.util.Optional;

/**
 * This class will be removed and replaced by the ErrorInfo class.
 */
@Deprecated
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

    public ErrorInfoWire() {}  // Required for ObjectMapper

    public ErrorInfoWire(String code, String message, ConstraintFailureWire[] constraintFailures, String abort) {
        this.code = code;
        this.message = message;
    }



    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Optional<ConstraintFailureWire[]> getConstraintFailures() {
        return Optional.ofNullable(constraintFailures);
    }

    public Optional<ConstraintFailure[]> getConstraintFailureArray() {
        if (constraintFailures == null) {
            return Optional.empty();
        } else {
            ConstraintFailure[] failures = new ConstraintFailure[constraintFailures.length];
            for (int i = 0; i < constraintFailures.length; i++) {
                failures[i] = this.constraintFailures[i].toConstraintFailure();
            }
            return Optional.of(failures);
        }
    }


    public Optional<String> getAbort() {
        return Optional.ofNullable(this.abort);
    }
}
