package com.fauna.constants;

/**
 * Defines standard error messages used throughout the Fauna client.
 *
 * <p>The {@code ErrorMessages} class centralizes error message constants for common
 * operations, allowing for consistent messaging across the client and simplifying maintenance.</p>
 */
public final class ErrorMessages {

    private ErrorMessages() {
    }

    /**
     * Error message indicating a query execution failure.
     */
    public static final String QUERY_EXECUTION = "Unable to execute query.";

    /**
     * Error message indicating a failure to subscribe to an <a href="https://docs.fauna.com/fauna/current/reference/cdc/#event-streaming">Event Stream</a>.
     */
    public static final String STREAM_SUBSCRIPTION = "Unable to subscribe to stream.";

    /**
     * Error message indicating a failure to subscribe to an <a href="https://docs.fauna.com/fauna/current/reference/cdc/#event-feeds">Event Feed</a>.
     */
    public static final String FEED_SUBSCRIPTION = "Unable to subscribe to feed.";

    /**
     * Error message indicating a failure to query a page of data.
     */
    public static final String QUERY_PAGE = "Unable to query page.";
}
