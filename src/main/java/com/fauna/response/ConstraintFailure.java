package com.fauna.response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fauna.constants.ResponseFields;
import com.fauna.exception.ClientException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConstraintFailure {
    private final String message;

    private final String name;

    private final PathElement[][] paths;

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
        JsonToken firstToken = parser.nextToken();
        if (firstToken == JsonToken.VALUE_NULL) {
            return null;
        } else if (firstToken != JsonToken.START_OBJECT) {
            throw new ClientException("Constraint failure should be a JSON object or null, got" + firstToken);
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
                case ResponseFields.ERROR_PATHS_FIELD_NAME:
                    List<PathElement[]> paths = new ArrayList<>();
                    JsonToken firstPathToken = parser.nextToken();
                    if (firstPathToken != JsonToken.START_ARRAY || firstPathToken != JsonToken.VALUE_NULL) {
                        throw new ClientException("Constraint failure should be array or null, got: " + firstPathToken.toString());
                    }
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        List<PathElement> path = new ArrayList<>();
                        if (parser.nextToken() == JsonToken.START_ARRAY) {
                            JsonToken pathToken = parser.nextToken();
                            while (pathToken != JsonToken.END_ARRAY) {
                                path.add(PathElement.parse(parser));
                            }
                        }
                        paths.add(path.toArray(new PathElement[path.size()]));
                    }
                    builder.paths(paths.toArray(new PathElement[paths.size()][]));
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

    public PathElement[][] getPaths() {
        return paths;
    }

}
