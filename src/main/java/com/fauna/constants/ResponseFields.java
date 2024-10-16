package com.fauna.constants;

public final class ResponseFields {

    // Top-level fields
    public static final String DATA_FIELD_NAME = "data";
    public static final String LAST_SEEN_TXN_FIELD_NAME = "txn_ts";
    public static final String STATIC_TYPE_FIELD_NAME = "static_type";
    public static final String STATS_FIELD_NAME = "stats";
    public static final String SCHEMA_VERSION_FIELD_NAME = "schema_version";
    public static final String SUMMARY_FIELD_NAME = "summary";
    public static final String QUERY_TAGS_FIELD_NAME = "query_tags";
    public static final String ERROR_FIELD_NAME = "error";

    // "stats" block
    public static final String STATS_COMPUTE_OPS_FIELD_NAME = "compute_ops";
    public static final String STATS_READ_OPS = "read_ops";
    public static final String STATS_WRITE_OPS = "write_ops";
    public static final String STATS_QUERY_TIME_MS = "query_time_ms";
    public static final String STATS_PROCESSING_TIME_MS = "processing_time_ms";
    public static final String STATS_CONTENTION_RETRIES = "contention_retries";
    public static final String STATS_STORAGE_BYTES_READ = "storage_bytes_read";
    public static final String STATS_STORAGE_BYTES_WRITE = "storage_bytes_write";
    public static final String STATS_RATE_LIMITS_HIT = "rate_limits_hit";

    // "error" block
    public static final String ERROR_CODE_FIELD_NAME = "code";
    public static final String ERROR_MESSAGE_FIELD_NAME = "message";
    public static final String ERROR_CONSTRAINT_FAILURES_FIELD_NAME = "constraint_failures";
    public static final String ERROR_ABORT_FIELD_NAME = "abort";
    public static final String ERROR_NAME_FIELD_NAME = "name";
    public static final String ERROR_PATHS_FIELD_NAME = "paths";

    // Stream and Feed related fields
    public static final String CURSOR_FIELD_NAME = "cursor";

    // Stream-related fields
    public static final String STREAM_TYPE_FIELD_NAME = "type";

    // Feed-related fields
    public static final String EVENTS_FIELD_NAME = "events";
    public static final String FEED_HAS_NEXT_FIELD_NAME = "has_next";
}