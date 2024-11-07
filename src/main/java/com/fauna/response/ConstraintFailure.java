package com.fauna.response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fauna.constants.ResponseFields;
import com.fauna.exception.ClientResponseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ConstraintFailure {
    private final String message;

    private final String name;

    private final PathElement[][] paths;

    /**
     * Initialize a new ConstraintFailure instance. Queries that fail a <a
     * href="https://docs.fauna.com/fauna/current/reference/fsl/check/">check</a>
     * or <a
     * href="https://docs.fauna.com/fauna/current/reference/fsl/unique/">unique
     * constraint</a> return a constraint failure.
     *
     * @param message Human-readable description of the constraint failure.
     * @param name    Name of the failed constraint.
     * @param paths   A list of paths where the constraint failure occurred.
     */
    public ConstraintFailure(
            final String message,
            final String name,
            final PathElement[][] paths) {
        this.message = message;
        this.name = name;
        this.paths = paths;
    }

    /**
     * Constructs a PathElement[] from the provided objects. Supported types
     * are String and Integer.
     *
     * @param elements The String objects or Integer objects to use.
     * @return A array of PathElement instances.
     */
    public static PathElement[] createPath(final Object... elements) {
        List<PathElement> path = new ArrayList<>();
        for (Object element : elements) {
            if (element instanceof String) {
                path.add(new PathElement((String) element));
            } else if (element instanceof Integer) {
                path.add(new PathElement((Integer) element));
            } else {
                throw new IllegalArgumentException(
                        "Only strings and integers supported");
            }
        }
        return path.toArray(new PathElement[0]);
    }

    /**
     * Initializes a new empty Builder.
     *
     * @return A new Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builds a ConstraintFailure instance from the provided JsonParser.
     *
     * @param parser The JsonParser to consume.
     * @return A new ConstraintFailure instance.
     * @throws IOException Thrown if an error is encountered while reading the
     *                     parser.
     */
    public static ConstraintFailure parse(final JsonParser parser)
            throws IOException {
        if (parser.currentToken() != JsonToken.START_OBJECT
                && parser.nextToken() != JsonToken.START_OBJECT) {
            throw new ClientResponseException(
                    "Constraint failure should be a JSON object.");
        }
        Builder builder = ConstraintFailure.builder();
        while (parser.nextToken() == JsonToken.FIELD_NAME) {
            String fieldName = parser.getValueAsString();
            switch (fieldName) {
                case ResponseFields.ERROR_MESSAGE_FIELD_NAME:
                    builder.message(parser.nextTextValue());
                    break;
                case ResponseFields.ERROR_NAME_FIELD_NAME:
                    builder.name(parser.nextTextValue());
                    break;
                case ResponseFields.ERROR_PATHS_FIELD_NAME:
                    List<PathElement[]> paths = new ArrayList<>();
                    JsonToken firstPathToken = parser.nextToken();
                    if (firstPathToken == JsonToken.START_ARRAY) {
                        while (parser.nextToken() == JsonToken.START_ARRAY) {
                            List<PathElement> path = new ArrayList<>();
                            while (parser.nextToken() != JsonToken.END_ARRAY) {
                                path.add(PathElement.parse(parser));
                            }
                            paths.add(path.toArray(new PathElement[0]));
                        }
                    } else if (firstPathToken != JsonToken.VALUE_NULL) {
                        throw new ClientResponseException(
                                "Constraint failure path should be array or null, got: "
                                        + firstPathToken.toString());
                    }
                    paths.forEach(builder::path);
                    break;
                default:
            }
        }
        return builder.build();

    }

    /**
     * Gets the constraint failure message.
     *
     * @return A string representation of the message.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Gets the constraint failure name.
     *
     * @return A string representation of the name.
     */
    public Optional<String> getName() {
        return Optional.ofNullable(this.name);
    }

    /**
     * Gets an optional path elements related to the constraint failure.
     *
     * @return An array of arrays of PathElements.
     */
    public Optional<PathElement[][]> getPaths() {
        return Optional.ofNullable(paths);
    }

    /**
     * Gets a list of string representations of the constraint failure paths.
     *
     * @return A list of string representations of constraint failure paths.
     */
    public Optional<List<String>> getPathStrings() {
        if (paths == null) {
            return Optional.empty();
        } else {
            return Optional.of(Arrays.stream(paths).map(
                            pathElements -> Arrays.stream(pathElements)
                                    .map(PathElement::toString).collect(
                                            Collectors.joining(".")))
                    .collect(Collectors.toList()));
        }
    }

    /**
     * Tests path equality with another ConstraintFailure.
     *
     * @param otherFailure The other ConstraintFailure.
     * @return True if the paths are equal.
     */
    public boolean pathsAreEqual(final ConstraintFailure otherFailure) {
        PathElement[][] thisArray =
                this.getPaths().orElse(new PathElement[0][]);
        PathElement[][] otherArray =
                otherFailure.getPaths().orElse(new PathElement[0][]);
        return Arrays.deepEquals(thisArray, otherArray);
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof ConstraintFailure) {
            ConstraintFailure otherFailure = (ConstraintFailure) other;
            return this.getMessage().equals(otherFailure.getMessage())
                    && this.getName().equals(otherFailure.getName())
                    && pathsAreEqual(otherFailure);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                this.name,
                this.message,
                Arrays.deepHashCode(this.paths));
    }

    public static final class PathElement {
        private String sVal = null;
        private Integer iVal = null;

        /**
         * Initializes a PathElement with a string value.
         *
         * @param sVal The string value.
         */
        public PathElement(final String sVal) {
            this.sVal = sVal;
        }

        /**
         * Initializes a PathElement with an integer value.
         *
         * @param iVal The integer value.
         */
        public PathElement(final Integer iVal) {
            this.iVal = iVal;
        }

        /**
         * Note that this parse method does not advance the parser.
         *
         * @param parser A JsonParser instance.
         * @return A new PathElement.
         * @throws IOException Can be thrown if e.g. stream ends.
         */
        public static PathElement parse(final JsonParser parser)
                throws IOException {
            if (parser.currentToken().isNumeric()) {
                return new PathElement(parser.getValueAsInt());
            } else {
                return new PathElement(parser.getText());
            }
        }

        /**
         * Tests whether the PathElement stores a string or an integer.
         *
         * @return If it's a string, true. Otherwise, false.
         */
        public boolean isString() {
            return sVal != null;
        }

        @Override
        public boolean equals(final Object o) {
            if (o instanceof PathElement) {
                PathElement other = (PathElement) o;
                return other.isString() == this.isString()
                        && other.toString().equals(this.toString());
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        /**
         * Converts the PathElement to a string.
         *
         * @return A string representation of the PathElement.
         */
        public String toString() {
            return sVal == null ? String.valueOf(iVal) : sVal;
        }
    }

    public static class Builder {
        private final List<PathElement[]> paths = new ArrayList<>();
        private String message = null;
        private String name = null;

        /**
         * Sets a message on the builder.
         *
         * @param message The message to set.
         * @return this.
         */
        public Builder message(final String message) {
            this.message = message;
            return this;
        }

        /**
         * Sets a name on the builder.
         *
         * @param name The name to set.
         * @return this.
         */
        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets a path on the builder.
         *
         * @param path The path to set.
         * @return this.
         */
        public Builder path(final PathElement[] path) {
            this.paths.add(path);
            return this;
        }

        /**
         * Builds a ConstraintFailure instance from the current builder.
         *
         * @return A ConstraintFailure instance.
         */
        public ConstraintFailure build() {
            PathElement[][] paths =
                    this.paths.toArray(new PathElement[this.paths.size()][]);
            return new ConstraintFailure(this.message, this.name,
                    this.paths.isEmpty() ? null : paths);
        }

    }

}
