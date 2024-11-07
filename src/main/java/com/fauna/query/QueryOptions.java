package com.fauna.query;

import java.time.Duration;
import java.util.Optional;

import static com.fauna.constants.Defaults.DEFAULT_TIMEOUT;

/**
 * Encapsulates options for configuring Fauna queries, such as timeout,
 * linearized reads, typechecking, query tags, and trace parent for
 * distributed tracing.
 */
public class QueryOptions {
    private final Boolean linearized;
    private final Boolean typeCheck;
    private final Duration timeout;
    private final QueryTags queryTags;
    private final String traceParent;

    /**
     * Creates an instance of QueryOptions using the specified builder.
     *
     * @param builder the builder with values for query options.
     */
    public QueryOptions(final Builder builder) {
        this.linearized = builder.linearized;
        this.typeCheck = builder.typeCheck;
        this.timeout = builder.timeout;
        this.queryTags = builder.queryTags;
        this.traceParent = builder.traceParent;
    }

    /**
     * Default QueryOptions instance with default configurations.
     *
     * @return a new QueryOptions instance with defaults.
     */
    public static QueryOptions getDefault() {
        return QueryOptions.builder().build();
    }

    /**
     * Returns an Optional indicating if linearized reads are enabled.
     *
     * @return an Optional containing the linearized setting, or empty if not
     * specified.
     */
    public Optional<Boolean> getLinearized() {
        return Optional.ofNullable(this.linearized);
    }

    /**
     * Returns an Optional indicating if type checking is enabled.
     *
     * @return an Optional containing the typeCheck setting, or empty if not
     * specified.
     */
    public Optional<Boolean> getTypeCheck() {
        return Optional.ofNullable(this.typeCheck);
    }

    /**
     * Returns an Optional of the query timeout duration in milliseconds.
     *
     * @return an Optional containing the query timeout duration in milliseconds, or
     * empty if not specified.
     */
    public Optional<Long> getTimeoutMillis() {
        return Optional.ofNullable(this.timeout).map(Duration::toMillis);
    }

    /**
     * Returns an Optional of the query tags.
     *
     * @return an Optional containing the QueryTags, or empty if not specified.
     */
    public Optional<QueryTags> getQueryTags() {
        return Optional.ofNullable(this.queryTags);
    }

    /**
     * Returns an Optional of the trace parent for distributed tracing.
     *
     * @return an Optional containing the traceParent, or empty if not
     * specified.
     */
    public Optional<String> getTraceParent() {
        return Optional.ofNullable(this.traceParent);
    }

    /**
     * Builder class for constructing instances of QueryOptions.
     */
    public static class Builder {
        private Boolean linearized = null;
        private Boolean typeCheck = null;
        private Duration timeout = DEFAULT_TIMEOUT;
        private QueryTags queryTags = null;
        private String traceParent = null;

        /**
         * If true, read-only transactions that don't read indexes are <a
         * href="https://docs.fauna.com/fauna/current/learn/transactions/">strictly
         * serialized</a>.
         *
         * @param linearized true to enable linearized reads, false otherwise.
         * @return this Builder instance for chaining.
         */
        public Builder linearized(final boolean linearized) {
            this.linearized = linearized;
            return this;
        }

        /**
         * If true, <a href="/fauna/current/learn/query/static-typing/">typechecking</a>
         * is enabled for queries. You can only enable typechecking for databases that
         * have typechecking enabled.
         *
         * @param typeCheck true to enable type checking, false otherwise.
         * @return this Builder instance for chaining.
         */
        public Builder typeCheck(final boolean typeCheck) {
            this.typeCheck = typeCheck;
            return this;
        }

        /**
         * Sets the timeout duration for the query.
         *
         * @param timeout the timeout Duration for the query.
         * @return this Builder instance for chaining.
         */
        public Builder timeout(final Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Sets <a href="https://docs.fauna.com/fauna/current/manage/query-logs/#tags">query tags</a> used to
         * instrument the query. You typically use query tags to monitor and debug query requests in
         * <a href="https://docs.fauna.com/fauna/current/manage/query-logs/">Fauna Logs</a>.
         *
         * @param queryTags the QueryTags to associate with the query.
         * @return this Builder instance for chaining.
         */
        public Builder queryTags(final QueryTags queryTags) {
            if (this.queryTags != null) {
                this.queryTags.putAll(queryTags);
            } else {
                this.queryTags = queryTags;
            }
            return this;
        }

        /**
         * Adds a single query tag to the existing tags.
         *
         * @param key the key of the query tag.
         * @param value the value of the query tag.
         * @return this Builder instance for chaining.
         */
        public Builder queryTag(final String key, final String value) {
            if (this.queryTags == null) {
                this.queryTags = new QueryTags();
            }
            this.queryTags.put(key, value);
            return this;
        }

        /**
         * Traceparent identifier used for distributed tracing. Passed by the drive in the `traceparent` header of <a
         * href="https://docs.fauna.com/fauna/current/reference/http/reference/core-api/#operation/query">Query
         * HTTP endpoint</a> requests. If you don’t include a traceparent identifier or use an invalid identifier,
         * Fauna generates a valid identifier.
         *
         * @param traceParent the trace parent ID.
         * @return this Builder instance for chaining.
         */
        public Builder traceParent(final String traceParent) {
            this.traceParent = traceParent;
            return this;
        }

        /**
         * Builds and returns a new instance of QueryOptions.
         *
         * @return a new QueryOptions instance with the configured settings.
         */
        public QueryOptions build() {
            return new QueryOptions(this);
        }

    }

    /**
     * Creates and returns a new Builder instance for constructing QueryOptions.
     *
     * @return a new Builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }
}
