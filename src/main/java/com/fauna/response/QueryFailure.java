package com.fauna.response;

import java.util.Map;
import java.util.Optional;

public final class QueryFailure extends QueryResponse {

    private final int statusCode;
    private final ErrorInfo errorInfo;

    public QueryFailure(int httpStatus, Builder builder) {
        super(builder);
        this.statusCode = httpStatus;
        this.errorInfo = builder.error;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorInfo.getCode();
    }

    public String getMessage() {
        return errorInfo.getMessage();
    }

    public <T> Optional<T> getAbort(Class<T> clazz) {
        return errorInfo.getAbort(clazz);

    }

    public String getFullMessage() {
        String summarySuffix = this.getSummary() != null ? "\n---\n" + this.getSummary() : "";
        return String.format("%d (%s): %s%s",
                this.getStatusCode(), this.getErrorCode(), this.getMessage(), summarySuffix);

    }

    public Optional<ConstraintFailure[]> getConstraintFailures() {
        return this.errorInfo.getConstraintFailures();
    }
}
