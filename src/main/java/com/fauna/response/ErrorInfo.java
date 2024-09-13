package com.fauna.response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fauna.exception.ClientException;
import com.fauna.exception.ClientResponseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.fauna.constants.ResponseFields.ERROR_CODE_FIELD_NAME;
import static com.fauna.constants.ResponseFields.ERROR_CONSTRAINT_FAILURES_FIELD_NAME;
import static com.fauna.constants.ResponseFields.ERROR_MESSAGE_FIELD_NAME;

/**
 * This class will encapsulate all the information Fauna returns about errors including constraint failures, and
 * abort data, for now it just has the code and message.
 */
public class ErrorInfo {
    private final String code;
    private final String message;
    private final ConstraintFailure[] constraintFailures;
    private final String abort;

    public ErrorInfo(String code, String message, ConstraintFailure[] constraintFailures, String abort) {
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

    public Optional<ConstraintFailure[]> getConstraintFailures() {
        return Optional.ofNullable(this.constraintFailures);
    }
    public String getAbort() {
        return this.abort;
    }

    public static class Builder {
        String code = null;
        String message = null;
        ConstraintFailure[] constraintFailures = null;
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

        public Builder constraintFailures(List<ConstraintFailure> constraintFailures) {
            this.constraintFailures = constraintFailures.toArray(new ConstraintFailure[constraintFailures.size()]);
            return this;
        }

        public ErrorInfo build() {
            return new ErrorInfo(this.code, this.message, this.constraintFailures, "abort parsing not implemented");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ErrorInfo parse(JsonParser parser) throws IOException {
        if (parser.nextToken() != JsonToken.START_OBJECT) {
            throw new ClientException("Error parsing error info, got token" + parser.currentToken());
        }
        Builder builder = ErrorInfo.builder();

        while (parser.nextToken() == JsonToken.FIELD_NAME) {
            String fieldName = parser.getCurrentName();
            switch (fieldName) {
                case ERROR_CODE_FIELD_NAME:
                    builder.code(parser.nextTextValue());
                    break;
                case ERROR_MESSAGE_FIELD_NAME:
                    builder.message(parser.nextTextValue());
                    break;
                case ERROR_CONSTRAINT_FAILURES_FIELD_NAME:
                    List<ConstraintFailure> failures = new ArrayList<>();
                    JsonToken token = parser.nextToken();
                    if (token == JsonToken.VALUE_NULL) {
                        break;
                    } else if (token == JsonToken.START_ARRAY) {
                        JsonToken nextToken = parser.nextToken();
                        while (nextToken == JsonToken.START_OBJECT) {
                            failures.add(ConstraintFailure.parse(parser));
                            nextToken = parser.nextToken();
                        }
                        builder.constraintFailures(failures);
                        break;
                    } else {
                        throw new ClientException("Unexpected for constraint failures: " + token);
                    }
                default: throw new ClientResponseException("Unexpected token in error info: " + parser.currentToken());
            }
        }
        return builder.build();
    }
}
