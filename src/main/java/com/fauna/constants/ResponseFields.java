package com.fauna.constants;

/**
 * Defines constants for field names in responses returned by the <a href="https://docs.fauna.com/fauna/current/reference/http/reference/core-api/">Fauna Core HTTP API</a>.
 *
 * <p>The {@code ResponseFields} class centralizes commonly used JSON field names in Fauna responses,
 * making it easier to reference them consistently and preventing hard-coded strings throughout the codebase.</p>
 */
public final class ResponseFields {

    private ResponseFields() {
    }

    // Top-level fields
    /**
     * Field name for data returned in a response.
     */
    public static final String DATA_FIELD_NAME = "data";

    /**
     * Field name for the last seen transaction timestamp.
     */
    public static final String LAST_SEEN_TXN_FIELD_NAME = "txn_ts";

    /**
     * Field name for the static type of the response data.
     */
    public static final String STATIC_TYPE_FIELD_NAME = "static_type";

    /**
     * Field name for <a href="https://docs.fauna.com/fauna/current/reference/http/reference/query-stats/">query and event stats</a> in the response.
     */
    public static final String STATS_FIELD_NAME = "stats";

    /**
     * Field name for the <a href="https://docs.fauna.com/fauna/current/learn/schema/#version">database schema version</a> in the response.
     */
    public static final String SCHEMA_VERSION_FIELD_NAME = "schema_version";

    /**
     * Field name for the summary information in the response.
     */
    public static final String SUMMARY_FIELD_NAME = "summary";

    /**
     * Field name for <a href="https://docs.fauna.com/fauna/current/manage/query-logs/#tags">query tags</a> included in the response.
     */
    public static final String QUERY_TAGS_FIELD_NAME = "query_tags";

    /**
     * Field name for error information in the response.
     */
    public static final String ERROR_FIELD_NAME = "error";

    // "stats" block
    /**
     * Field name for compute operation statistics.
     */
    public static final String STATS_COMPUTE_OPS_FIELD_NAME = "compute_ops";

    /**
     * Field name for read operation statistics.
     */
    public static final String STATS_READ_OPS = "read_ops";

    /**
     * Field name for write operation statistics.
     */
    public static final String STATS_WRITE_OPS = "write_ops";

    /**
     * Field name for the query runtime in milliseconds.
     */
    public static final String STATS_QUERY_TIME_MS = "query_time_ms";

    /**
     * Field name for event processing time in milliseconds.
     */
    public static final String STATS_PROCESSING_TIME_MS = "processing_time_ms";

    /**
     * Field name for <a href="https://docs.fauna.com/fauna/current/learn/transactions/contention/#retries">transaction contention retries</a> count.
     */
    public static final String STATS_CONTENTION_RETRIES = "contention_retries";

    /**
     * Field name for data read from storage, in bytes.
     */
    public static final String STATS_STORAGE_BYTES_READ = "storage_bytes_read";

    /**
     * Field name for data written to storage, in bytes.
     */
    public static final String STATS_STORAGE_BYTES_WRITE = "storage_bytes_write";

    /**
     * Field name for rate limit hits.
     */
    public static final String STATS_RATE_LIMITS_HIT = "rate_limits_hit";

    // "error" block
    /**
     * Field name for the <a href="https://docs.fauna.com/fauna/current/reference/http/reference/errors/#error-codes">error code</a> in the response.
     */
    public static final String ERROR_CODE_FIELD_NAME = "code";

    /**
     * Field name for the error message in the response.
     */
    public static final String ERROR_MESSAGE_FIELD_NAME = "message";

    /**
     * Field name for <a href="https://docs.fauna.com/fauna/current/reference/http/reference/errors/#constraints">constraint failures</a> in error information.
     */
    public static final String ERROR_CONSTRAINT_FAILURES_FIELD_NAME = "constraint_failures";

    /**
     * Field name for <a href="https://docs.fauna.com/fauna/current/reference/http/reference/errors/#abort">abort error</a> information in the error response.
     */
    public static final String ERROR_ABORT_FIELD_NAME = "abort";

    /**
     * Field name for the error name in the response.
     */
    public static final String ERROR_NAME_FIELD_NAME = "name";

    /**
     * Field name for paths involved in the error.
     */
    public static final String ERROR_PATHS_FIELD_NAME = "paths";

    // Event-related fields
    /**
     * Field name for the cursor in stream and feed responses.
     */
    public static final String CURSOR_FIELD_NAME = "cursor";
    /**
     * Field name for the event type in <a href="https://docs.fauna.com/fauna/current/reference/cdc/#event-feeds">Event Feed</a> and <a href="https://docs.fauna.com/fauna/current/reference/cdc/#event-streaming">Event Stream</a> responses.
     */
    public static final String STREAM_TYPE_FIELD_NAME = "type";

    // Feed-related fields
    /**
     * Field name for events in <a href="https://docs.fauna.com/fauna/current/reference/cdc/#event-feeds">Event Feed</a> responses.
     */
    public static final String EVENTS_FIELD_NAME = "events";

    /**
     * Field name indicating whether there are more pages in <a href="https://docs.fauna.com/fauna/current/reference/cdc/#event-feeds">Event Feed</a> responses.
     */
    public static final String FEED_HAS_NEXT_FIELD_NAME = "has_next";
}
