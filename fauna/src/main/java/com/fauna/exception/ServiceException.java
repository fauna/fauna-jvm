package com.fauna.exception;

import com.fauna.response.QueryFailure;
import com.fauna.response.QueryStats;

import java.util.Map;

public class ServiceException extends FaunaException {
    private final QueryFailure response;

    public ServiceException(QueryFailure response) {
        super(response.getMessage());
        this.response = response;
    }

    public QueryFailure getResponse() {
        return this.response;
    }

    public int getStatusCode() {
        return this.response.getStatusCode();
    }

    public String getErrorCode() {
        return this.response.getErrorCode();
    }

    public String getSummary() {
        return this.response.getSummary();
    }

    public QueryStats getStats() {
        return this.response.getStats();
    }

    public long getTxnTs() {
        return this.response.getLastSeenTxn();
    }

    public long getSchemaVersion() {
        return this.response.getSchemaVersion();
    }

    public Map<String, String> getQueryTags() {
        return this.response.getQueryTags();
    }

}
