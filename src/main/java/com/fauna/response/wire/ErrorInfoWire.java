package com.fauna.response.wire;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fauna.codec.json.PassThroughDeserializer;
import com.fauna.exception.ClientException;
import com.fauna.response.ConstraintFailure;

import static com.fauna.constants.ResponseFields.ERROR_CODE_FIELD_NAME;
import static com.fauna.constants.ResponseFields.ERROR_CONSTRAINT_FAILURES_FIELD_NAME;
import static com.fauna.constants.ResponseFields.ERROR_MESSAGE_FIELD_NAME;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    public static class Builder {
        String code = null;
        String message = "";
        ConstraintFailureWire[] constraintFailures = null;
        String abort = null;

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder abort(Object abort) {
            this.abort = abort.toString();
            return this;
        }

        public ErrorInfoWire build() {
            return new ErrorInfoWire(this.code, this.message, this.constraintFailures, "abort parsing not implemented");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ErrorInfoWire parse(JsonParser parser) throws IOException {
        if (parser.nextToken() != JsonToken.START_OBJECT) {
            throw new ClientException("Error parsing error info, got token" + parser.currentToken());
        }
        Builder builder = ErrorInfoWire.builder();

        while (parser.nextToken() != JsonToken.FIELD_NAME) {
            String fieldName = parser.getCurrentName();
            switch (fieldName) {
                case ERROR_CODE_FIELD_NAME:
                    parser.nextToken();
                    builder.code(parser.getText());
                    break;
                case ERROR_MESSAGE_FIELD_NAME:
                    parser.nextToken();
                    builder.message(parser.getText());
                    break;
                case ERROR_CONSTRAINT_FAILURES_FIELD_NAME:
                    List<ConstraintFailure> failures = new ArrayList<>();
                    JsonToken token = parser.nextToken();
                    if (token == JsonToken.VALUE_NULL) {
                        break;
                    } else if (token == JsonToken.START_ARRAY) {
                        while (parser.nextToken() == JsonToken.START_OBJECT) {
                            failures.add(ConstraintFailure.parse(parser));
                        }
                        break;
                    } else {
                        throw new ClientException("Unexpected for constraint failures: " + token);
                    }
            }
        }
        return builder.build();
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

    public Optional<String> getAbort() {
        return Optional.ofNullable(this.abort);
    }
}
