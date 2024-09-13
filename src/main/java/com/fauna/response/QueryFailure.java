package com.fauna.response;

import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.ClientException;
import com.fauna.exception.ClientResponseException;
import com.fauna.exception.CodecException;
import com.fauna.response.wire.ConstraintFailureWire;
import com.fauna.response.wire.ErrorInfoWire;
import com.fauna.response.wire.QueryResponseWire;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class QueryFailure extends QueryResponse {

    private final int statusCode;
    private final ErrorInfo errorInfo;
    private String errorCode = "";
    private String message = "";

    private List<ConstraintFailure> constraintFailures;
    private String abortString;


    public QueryFailure(int statusCode, ErrorInfo errorInfo, Long schemaVersion, Map<String, String> queryTags, QueryStats stats) {
        super(null, null, schemaVersion, queryTags, stats);
        this.statusCode = statusCode;
        this.errorInfo = errorInfo;
    }

    /**
     * Initializes a new instance of the {@link QueryFailure} class, parsing the provided raw
     * response to extract error information.
     *
     * @param statusCode The HTTP status code.
     * @param response   The parsed response.
     */
    public QueryFailure(int statusCode, QueryResponseWire response) {
        super(response.getTxnTs(), response.getSummary(), response.getSchemaVersion(), response.getQueryTags(), response.getStats());
        ErrorInfoWire errorInfoWire = response.getError();
        if (errorInfoWire != null) {
            this.errorInfo = new ErrorInfo(errorCode, errorInfoWire.getMessage(), errorInfoWire.getConstraintFailureArray().orElse(null), errorInfoWire.getAbort().orElse(null));
        } else {
            this.errorInfo = new ErrorInfo(errorCode, null, null, null);
        }

        this.statusCode = statusCode;

        var err = response.getError();
        if (err != null) {
            errorCode = err.getCode();
            message = err.getMessage();

            if (err.getConstraintFailures().isPresent()) {
                var cf = new ArrayList<ConstraintFailure>();
                var codec = DefaultCodecProvider.SINGLETON.get(Object.class);
                for (ConstraintFailureWire cfw : err.getConstraintFailures().get()) {
                    try {
                        var parser = UTF8FaunaParser.fromString(cfw.getPaths());
                        var paths = codec.decode(parser);
                        cf.add(new ConstraintFailure(cfw.getMessage(), cfw.getName(), (List<List<Object>>) paths));
                    } catch (CodecException exc) {
                        throw new ClientResponseException("Failed to parse constraint failure.", exc);
                    }
                }
                constraintFailures = cf;
            }

            if (err.getAbort().isPresent()) {
                abortString = err.getAbort().get();
            }
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

    public String getFullMessage() {
        String summarySuffix = this.getSummary() != null ? "\n---\n" + this.getSummary() : "";
        return String.format("%d (%s): %s%s",
                this.getStatusCode(), this.getErrorCode(), this.getMessage(), summarySuffix);

    }

    public Optional<List<ConstraintFailure>> getConstraintFailures() {
        return Optional.ofNullable(constraintFailures);
    }

    public Optional<String> getAbortString() {
        return Optional.ofNullable(this.abortString);
    }
}
