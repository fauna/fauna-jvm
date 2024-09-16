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

    // This constructor is called by the QueryFailure constructor, which is getting deprecated.
    @Deprecated
    public ConstraintFailure(String message, String name, List<List<Object>> pathLists) {
        this.message = message;
        this.name = name;
        this.paths = new PathElement[pathLists.size()][];
        for (int i = 0; i < pathLists.size(); i++) {
            this.paths[i] = new PathElement[pathLists.get(i).size()];
            for (int j = 0; j < this.paths[i].length; j++) {
                Object element = pathLists.get(i).get(j);
                if (element instanceof String) {
                    paths[i][j] = new PathElement((String) element);
                } else if (element instanceof Integer) {
                    paths[i][j] = new PathElement((Integer) element);
                }
            }
        }
    }

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
         * Note that this parser does not advance the parser.
         * @param parser
         * @return
         * @throws IOException
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


    public static class Builder {
        String message = null;
        String name = null;
        PathElement[][] paths;

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder paths(PathElement[][] paths) {
            this.paths = paths;
            return this;
        }

        public ConstraintFailure build() {
            return new ConstraintFailure(this.message, this.name, this.paths);
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
                            paths.add(path.toArray(new PathElement[path.size()]));
                        }
                        builder.paths(paths.toArray(new PathElement[paths.size()][]));
                    } else if (firstPathToken != JsonToken.VALUE_NULL) {
                        throw new ClientResponseException("Constraint failure path should be array or null, got: " + firstPathToken.toString());
                    }
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

}
