package com.fauna.response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.ClientResponseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.fauna.constants.ResponseFields.ERROR_ABORT_FIELD_NAME;
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
    private final TreeNode abort;

    public ErrorInfo(String code, String message, ConstraintFailure[] constraintFailures, TreeNode abort) {
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

    public Optional<TreeNode> getAbortJson() {
        return Optional.ofNullable(this.abort);
    }

    public <T> Optional<T> getAbort(Class<T> abortDataClass) {
        return this.getAbortJson().map(tree -> {
            UTF8FaunaParser parser = new UTF8FaunaParser(tree.traverse());
            Codec<T> codec = DefaultCodecProvider.SINGLETON.get(abortDataClass);
            parser.read();
            return codec.decode(parser);
        });
    }



    public static class Builder {
        String code = null;
        String message = null;
        ConstraintFailure[] constraintFailures = null;
        TreeNode abort = null;

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder abort(TreeNode abort) {
            this.abort = abort;
            return this;
        }

        public Builder constraintFailures(List<ConstraintFailure> constraintFailures) {
            this.constraintFailures = constraintFailures.toArray(new ConstraintFailure[0]);
            return this;
        }

        public ErrorInfo build() {
            return new ErrorInfo(this.code, this.message, this.constraintFailures, this.abort);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ErrorInfo parse(JsonParser parser) throws IOException {
        if (parser.nextToken() != JsonToken.START_OBJECT) {
            throw new ClientResponseException("Error parsing error info, got token" + parser.currentToken());
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
                case ERROR_ABORT_FIELD_NAME:
                    parser.nextToken();
                    builder.abort(new ObjectMapper().readTree(parser));
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
                        throw new ClientResponseException("Unexpected token in constraint failures: " + token);
                    }
                default: throw new ClientResponseException("Unexpected token in error info: " + parser.currentToken());
            }
        }
        return builder.build();
    }
}
