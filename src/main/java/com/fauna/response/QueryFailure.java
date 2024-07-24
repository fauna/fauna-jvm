package com.fauna.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fauna.common.constants.ResponseFields;

import java.util.Optional;

public final class QueryFailure extends QueryResponse {

    private static ObjectMapper fallbackMapper = new ObjectMapper();
    private int statusCode;
    private String errorCode = "";
    private String message = "";
    private Object constraintFailures;
    private Object abort;

    public static QueryFailure fallback() {
        try {
            return new QueryFailure(500, fallbackMapper.createObjectNode(), null);
        } catch (JsonProcessingException exc) {
            throw new RuntimeException(exc);
        }
    }

    /**
     * Initializes a new instance of the {@link QueryFailure} class, parsing the provided raw
     * response text to extract error information.
     *
     * @param statusCode The HTTP status code.
     * @param json       The JSON response body.
     */
    public QueryFailure(int statusCode, JsonNode json, QueryStats stats) throws JsonProcessingException {
        super(json, stats);
        this.statusCode = statusCode;

        JsonNode elem;

        if ((elem = json.get(ResponseFields.ERROR_FIELD_NAME)) != null) {

            ErrorInfo info = new ObjectMapper().treeToValue(elem, ErrorInfo.class);
            errorCode = info.getCode() != null ? info.getCode() : "";
            message = info.getMessage() != null ? info.getMessage() : "";
            constraintFailures = info.getConstraintFailures();
            abort = info.getAbort().isPresent() ? info.getAbort().get() : null;

        }
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public Object getConstraintFailures() {
        return constraintFailures;
    }

    public Optional<Object> getAbort() {
        return Optional.ofNullable(this.abort);
    }
}
