package com.fauna.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

public final class QueryFailure extends QueryResponse {

    private static ObjectMapper fallbackMapper = new ObjectMapper();
    private int statusCode;
    private String errorCode = "";
    private String message = "";
    private ConstraintFailure[] constraintFailures;
    private String abortRaw;

    private String fullMessage = "";

    public static QueryFailure fallback() {
        try {
            return new QueryFailure(500, new QueryResponseInternal());
        } catch (JsonProcessingException exc) {
            throw new RuntimeException(exc);
        }
    }

    /**
     * Initializes a new instance of the {@link QueryFailure} class, parsing the provided raw
     * response to extract error information.
     *
     * @param statusCode The HTTP status code.
     * @param response   The parsed response.
     */
    public QueryFailure(int statusCode, QueryResponseInternal response) throws JsonProcessingException {
        super(response);
        this.statusCode = statusCode;

        if (response.error != null) {
            errorCode = response.error.getCode();
            message = response.error.getMessage();
            constraintFailures = response.error.getConstraintFailures();
            abortRaw = response.error.getAbortRaw();
        }

        var maybeSummary = !this.getSummary().isEmpty() ? "\n---\n" + this.getSummary() : "";
        fullMessage = String.format(
                "%d (%s): %s%s",
                this.getStatusCode(),
                this.getErrorCode(),
                this.getMessage(),
                maybeSummary);

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

    public String getFullMessage() {
        return fullMessage;
    }

    public Optional<ConstraintFailure[]> getConstraintFailures() {
        return Optional.ofNullable(constraintFailures);
    }

    public Optional<String> getAbortRaw() {
        return Optional.ofNullable(this.abortRaw);
    }
}
