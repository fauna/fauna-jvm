package com.fauna.exception;

import com.fauna.response.QueryFailure;
import com.fauna.response.QueryStats;

import java.util.Map;
import java.util.Optional;

/**
 * An exception representing a query failure returned by Fauna.
 *
 * <p>This exception extends {@link FaunaException} and provides detailed information
 * about the failed query, including HTTP status codes, error codes, statistics,
 * and other metadata.</p>
 */
public class ServiceException extends FaunaException {
    private final QueryFailure response;

    /**
     * Constructs a new {@code ServiceException} with the specified {@code QueryFailure} response.
     *
     * @param response The {@code QueryFailure} object containing details about the failed query.
     */
    public ServiceException(final QueryFailure response) {
        super(response.getFullMessage());
        this.response = response;
    }

    /**
     * Returns the {@link QueryFailure} response associated with this exception.
     *
     * @return The {@code QueryFailure} object containing details of the query failure.
     */
    public QueryFailure getResponse() {
        return this.response;
    }

    /**
     * Returns the HTTP status code of the response returned by the query request.
     *
     * @return The HTTP status code as an integer.
     */
    public int getStatusCode() {
        return this.response.getStatusCode();
    }

    /**
     * Returns the <a href="https://docs.fauna.com/fauna/current/reference/fql/error-codes/">
     * Fauna error code</a> associated with the failure.
     *
     * <p>Fauna error codes indicate the specific cause of the error and are part of the API contract,
     * allowing for programmatic logic based on the error type.</p>
     *
     * @return The error code as a {@code String}.
     */
    public String getErrorCode() {
        return this.response.getErrorCode();
    }

    /**
     * Returns a brief summary of the error.
     *
     * @return A {@code String} containing the error summary.
     */
    public String getSummary() {
        return this.response.getSummary();
    }

    /**
     * Returns the statistics associated with the failed query.
     *
     * @return A {@link QueryStats} object containing statistical information for the failed query.
     */
    public QueryStats getStats() {
        return this.response.getStats();
    }

    /**
     * Returns the last transaction timestamp seen for the failed query, if available.
     *
     * @return An {@code Optional<Long>} representing the last transaction timestamp, or {@code Optional.empty()} if not available.
     */
    public Optional<Long> getTxnTs() {
        return Optional.ofNullable(this.response.getLastSeenTxn());
    }

    /**
     * Returns the schema version used during query execution.
     *
     * @return The schema version as a {@code Long} value.
     */
    public Long getSchemaVersion() {
        return this.response.getSchemaVersion();
    }

    /**
     * Returns a map of query tags for the failed query, containing key-value pairs of tags.
     *
     * @return A {@code Map<String, String>} with query tags.
     */
    public Map<String, String> getQueryTags() {
        return this.response.getQueryTags();
    }
}
