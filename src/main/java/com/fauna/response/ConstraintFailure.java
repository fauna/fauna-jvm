package com.fauna.response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fauna.constants.ResponseFields;
import com.fauna.exception.ClientResponseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConstraintFailure {
    private final String message;

    private final String name;

    private final PathElement[][] paths;

    public ConstraintFailure(String message, String name, PathElement[][] paths) {
        this.message = message;
        this.name = name;
        this.paths = paths;
    }

    public static class PathElement {
        private String sVal = null;
        private Integer iVal = null;

        public PathElement(String sVal) {
            this.sVal = sVal;
        }

        public PathElement(Integer iVal) {
            this.iVal = iVal;
        }

        public boolean isString() {
            return sVal != null;
        }

        /**
         * Note that this parse method does not advance the parser.
         * @param parser        A JsonParser instance.
         * @return              A new PathElement.
         * @throws IOException  Can be thrown if e.g. stream ends.
         */
        public static PathElement parse(JsonParser parser) throws IOException {
            if (parser.currentToken().isNumeric()) {
                return new PathElement(parser.getValueAsInt());
            } else {
                return new PathElement(parser.getText());
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof PathElement) {
                PathElement other = (PathElement) o;
                return other.isString() == this.isString() && other.toString().equals(this.toString());
            } else {
                return false;
            }
        }

        public String toString() {
            return sVal == null ? String.valueOf(iVal) : sVal;
        }
    }

    public static PathElement[] createPath(Object... elements) {
        List<PathElement> path = new ArrayList<>();
        for (Object element : elements) {
            if (element instanceof String) {
                path.add(new PathElement((String) element));
            } else if (element instanceof Integer) {
                path.add(new PathElement((Integer) element));
            } else {
                throw new IllegalArgumentException("Only strings and integers supported");
            }
        }
        return path.toArray(new PathElement[0]);
    }


    public static class Builder {
        String message = null;
        String name = null;
        List<PathElement[]> paths = new ArrayList<>();

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder path(PathElement[] path) {
            this.paths.add(path);
            return this;
        }

        public ConstraintFailure build() {
            PathElement[][] paths = this.paths.toArray(new PathElement[this.paths.size()][]);
            return new ConstraintFailure(this.message, this.name, this.paths.isEmpty() ? null : paths);
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    public static ConstraintFailure parse(JsonParser parser) throws IOException {
        if (parser.currentToken() != JsonToken.START_OBJECT && parser.nextToken() != JsonToken.START_OBJECT) {
            throw new ClientResponseException("Constraint failure should be a JSON object.");
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
                        throw new ClientResponseException("Constraint failure path should be array or null, got: " + firstPathToken.toString());
                    }
                    paths.forEach(builder::path);
                    break;
            }
        }
        return builder.build();

    }

    public String getMessage() {
        return this.message;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(this.name);
    }

    public Optional<PathElement[][]> getPaths() {
        return Optional.ofNullable(paths);
    }

    public Optional<List<String>> getPathStrings() {
        if (paths == null) {
            return Optional.empty();
        } else {
            return Optional.of(Arrays.stream(paths).map(
                    pathElements -> Arrays.stream(pathElements).map(PathElement::toString).collect(
                            Collectors.joining("."))).collect(Collectors.toList()));
        }
    }

    public boolean pathsAreEqual(ConstraintFailure otherFailure) {
        PathElement[][] thisArray = this.getPaths().orElse(new PathElement[0][]);
        PathElement[][] otherArray = otherFailure.getPaths().orElse(new PathElement[0][]);
        return Arrays.deepEquals(thisArray, otherArray);
    }

    public boolean equals(Object other) {
        if (other instanceof ConstraintFailure) {
            ConstraintFailure otherFailure = (ConstraintFailure) other;
            return this.getMessage().equals(otherFailure.getMessage())
                    && this.getName().equals(otherFailure.getName())
                    && pathsAreEqual(otherFailure);
        } else {
            return false;
        }
    }

}
