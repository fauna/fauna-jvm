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

    /**
     * Initializes a new ErrorInfo.
     *
     * @param code               The <a
     *                           href="https://docs.fauna.com/fauna/current/reference/http/reference/errors/#error-codes">Fauna
     *                           error code</a>.
     * @param message            A short, human-readable description of the
     *                           error.
     * @param constraintFailures The constraint failures for the error, if any.
     *                           Only present if the error code is
     *                           `constraint_failure`.
     * @param abort              A user-defined error message passed using an
     *                           FQL `abort()` method call. Only present if the error
     *                           code is `abort`.
     */
    public ErrorInfo(
            final String code,
            final String message,
            final ConstraintFailure[] constraintFailures,
            final TreeNode abort) {
        this.code = code;
        this.message = message;
        this.constraintFailures = constraintFailures;
        this.abort = abort;
    }

    /**
     * A utility method to instantiate an empty builder.
     *
     * @return A new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    private static Builder handleField(final Builder builder,
                                       final JsonParser parser)
            throws IOException {
        String fieldName = parser.getCurrentName();
        switch (fieldName) {
            case ERROR_CODE_FIELD_NAME:
                return builder.code(parser.nextTextValue());
            case ERROR_MESSAGE_FIELD_NAME:
                return builder.message(parser.nextTextValue());
            case ERROR_ABORT_FIELD_NAME:
                parser.nextToken();
                return builder.abort(new ObjectMapper().readTree(parser));
            case ERROR_CONSTRAINT_FAILURES_FIELD_NAME:
                List<ConstraintFailure> failures = new ArrayList<>();
                JsonToken token = parser.nextToken();
                if (token == JsonToken.VALUE_NULL) {
                    return builder;
                } else if (token == JsonToken.START_ARRAY) {
                    JsonToken nextToken = parser.nextToken();
                    while (nextToken == JsonToken.START_OBJECT) {
                        failures.add(ConstraintFailure.parse(parser));
                        nextToken = parser.nextToken();
                    }
                    return builder.constraintFailures(failures);
                } else {
                    throw new ClientResponseException(
                            "Unexpected token in constraint failures: "
                                    + token);
                }
            default:
                throw new ClientResponseException(
                        "Unexpected token in error info: "
                                + parser.currentToken());
        }
    }

    /**
     * Builds a new ErrorInfo from a JsonParser.
     *
     * @param parser The JsonParser to read.
     * @return A new ErrorInfo instance.
     * @throws IOException Thrown on errors reading from the parser.
     */
    public static ErrorInfo parse(final JsonParser parser) throws IOException {
        if (parser.nextToken() != JsonToken.START_OBJECT) {
            throw new ClientResponseException(
                    "Error parsing error info, got token"
                            + parser.currentToken());
        }
        Builder builder = ErrorInfo.builder();

        while (parser.nextToken() == JsonToken.FIELD_NAME) {
            builder = handleField(builder, parser);
        }
        return builder.build();
    }

    /**
     * Gets the Fauna error code.
     *
     * @return A string representing the Fauna error code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Gets the error message.
     *
     * @return A string representing the error message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the constraint failures.
     *
     * @return An optional containing the constraint failures.
     */
    public Optional<ConstraintFailure[]> getConstraintFailures() {
        return Optional.ofNullable(this.constraintFailures);
    }

    /**
     * Gets the user-defined abort error message as a JSON node.
     *
     * @return An optional TreeNode with the abort data.
     */
    public Optional<TreeNode> getAbortJson() {
        return Optional.ofNullable(this.abort);
    }

    /**
     * Parses the abort data into the provided class.
     *
     * @param abortDataClass The class to decode into.
     * @param <T>            The type to decode into.
     * @return An instance of the provided type.
     */
    public <T> Optional<T> getAbort(final Class<T> abortDataClass) {
        return this.getAbortJson().map(tree -> {
            UTF8FaunaParser parser = new UTF8FaunaParser(tree.traverse());
            Codec<T> codec = DefaultCodecProvider.SINGLETON.get(abortDataClass);
            parser.read();
            return codec.decode(parser);
        });
    }

    public static class Builder {
        private String code = null;
        private String message = null;
        private ConstraintFailure[] constraintFailures = null;
        private TreeNode abort = null;

        /**
         * Sets the error code on the builder.
         *
         * @param code The error code.
         * @return this
         */
        public Builder code(final String code) {
            this.code = code;
            return this;
        }

        /**
         * Sets the message on the builder.
         *
         * @param message The message.
         * @return this
         */
        public Builder message(final String message) {
            this.message = message;
            return this;
        }

        /**
         * Sets the abort data on the builder.
         *
         * @param abort The abort JSON node.
         * @return this
         */
        public Builder abort(final TreeNode abort) {
            this.abort = abort;
            return this;
        }

        /**
         * Sets the constraint failures on the builder.
         *
         * @param constraintFailures The constraint failures.
         * @return this
         */
        public Builder constraintFailures(
                final List<ConstraintFailure> constraintFailures) {
            this.constraintFailures =
                    constraintFailures.toArray(new ConstraintFailure[0]);
            return this;
        }

        /**
         * Returns a new ErrorInfo instance based on the current builder.
         *
         * @return An ErrorInfo instance
         */
        public ErrorInfo build() {
            return new ErrorInfo(this.code, this.message,
                    this.constraintFailures, this.abort);
        }
    }
}
