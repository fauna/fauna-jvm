package com.fauna.client;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public class QueryOptions {

    private Boolean linearized;
    private Boolean typeCheck;
    private Duration queryTimeout;
    private Map<String, String> queryTags;
    private String traceParent;

    public Boolean getLinearized() {
        return linearized;
    }

    public void setLinearized(Boolean linearized) {
        this.linearized = linearized;
    }

    public Boolean getTypeCheck() {
        return typeCheck;
    }

    public void setTypeCheck(Boolean typeCheck) {
        this.typeCheck = typeCheck;
    }

    public Duration getQueryTimeout() {
        return queryTimeout;
    }

    public void setQueryTimeout(Duration queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public Map<String, String> getQueryTags() {
        return queryTags;
    }

    public void setQueryTags(Map<String, String> queryTags) {
        this.queryTags = queryTags;
    }

    public String getTraceParent() {
        return traceParent;
    }

    public void setTraceParent(String traceParent) {
        this.traceParent = traceParent;
    }

    public static QueryOptions getFinalQueryOptions(QueryOptions defaultQueryOptions,
        QueryOptions queryOptionOverrides) {
        if (defaultQueryOptions == null && queryOptionOverrides == null) {
            return null;
        } else if (defaultQueryOptions == null) {
            return queryOptionOverrides;
        } else if (queryOptionOverrides == null) {
            return defaultQueryOptions;
        }

        QueryOptions finalQueryOptions = new QueryOptions();
        finalQueryOptions.setLinearized(defaultQueryOptions.getLinearized());
        finalQueryOptions.setTypeCheck(defaultQueryOptions.getTypeCheck());
        finalQueryOptions.setQueryTimeout(defaultQueryOptions.getQueryTimeout());
        finalQueryOptions.setQueryTags(defaultQueryOptions.getQueryTags());
        finalQueryOptions.setTraceParent(defaultQueryOptions.getTraceParent());

        if (queryOptionOverrides.getQueryTags() != null) {
            if (finalQueryOptions.getQueryTags() == null) {
                finalQueryOptions.setQueryTags(queryOptionOverrides.getQueryTags());
            } else {
                for (Map.Entry<String, String> entry : queryOptionOverrides.getQueryTags()
                    .entrySet()) {
                    finalQueryOptions.getQueryTags().put(entry.getKey(), entry.getValue());
                }
            }
        }

        return finalQueryOptions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QueryOptions that = (QueryOptions) o;
        return Objects.equals(linearized, that.linearized) && Objects.equals(typeCheck,
            that.typeCheck) && Objects.equals(queryTimeout, that.queryTimeout) && Objects.equals(
            queryTags, that.queryTags) && Objects.equals(traceParent, that.traceParent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(linearized, typeCheck, queryTimeout, queryTags, traceParent);
    }

    @Override
    public String toString() {
        return "QueryOptions{" +
            "linearized=" + linearized +
            ", typeCheck=" + typeCheck +
            ", queryTimeout=" + queryTimeout +
            ", queryTags=" + queryTags +
            ", traceParent='" + traceParent + '\'' +
            '}';
    }
}
