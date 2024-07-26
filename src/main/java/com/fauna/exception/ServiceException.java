package com.fauna.exception;

import java.util.Map;

import com.fauna.response.QueryFailure;
import com.fauna.response.QueryStats;

/**
 * An exception representing a query failure returned by Fauna.
 *
 * <p>The exception extends {@link FaunaException} and contains details about
 * the failed query, including HTTP status codes, error codes, and other
 * metadata.</p>
 */
public class ServiceException extends FaunaException {
    private final QueryFailure response;

    /**
     * Constructs a new ServiceException with the specified QueryFailure response.
     *
     * @param response the QueryFailure object containing details about the failed query
     */
    public ServiceException(QueryFailure response) {
        super(response.getMessage());
        this.response = response;
    }

    /**
     * Returns the QueryFailure response associated with the exception.
     *
     * @return the QueryFailure object
     */
    public QueryFailure getResponse() {
        return this.response;
    }

    /**
     * Returns the HTTP status code of the response returned by the query request.
     *
     * @return the HTTP status code as an integer
     */
    public int getStatusCode() {
        return this.response.getStatusCode();
    }

    /**
     * Returns the
     * <a href="https://docs.fauna.com/fauna/current/reference/fql/error-codes/">Fauna error code</a>
     * associated with the failure.
     *
     * <p>Codes indicate the cause of the error. It is safe to write
     * programmatic logic against the code. They are part of the API contract.</p>
     *
     * @return the error code as a String
     */
    public String getErrorCode() {
        return this.response.getErrorCode();
    }

    /**
     * Returns a summary of the error.
     *
     * @return a String containing the error summary
     */
    public String getSummary() {
        return this.response.getSummary();
    }

    /**
     * Returns the statistics for the failed query.
     *
     * @return a QueryStats object containing statistical information
     */
    public QueryStats getStats() {
        return this.response.getStats();
    }

    /**
     * Returns the faled query's last transaction timestamp.
     *
     * @return the transaction timestamp as a long value
     */
    public long getTxnTs() {
        return this.response.getLastSeenTxn();
    }

    /**
     * The schema version that was used for query execution.
     *
     * @return the schema version as a long value
     */
    public long getSchemaVersion() {
        return this.response.getSchemaVersion();
    }

    /**
     * Returns a map of query tags for the failed query.
     *
     * @return a Map containing query tags as key-value pairs
     */
    public Map<String, String> getQueryTags() {
        return this.response.getQueryTags();
    }
}
