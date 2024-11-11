/**
 * Provides classes for managing and interacting with <a href="https://docs.fauna.com/fauna/current/reference/cdc/#event-feeds">Event Feeds</a> and <a href="https://docs.fauna.com/fauna/current/reference/cdc/#event-streaming">Event Streams</a>.
 *
 * <p>This package includes core components and utilities for handling event streaming, 
 * such as options, requests, and response handling mechanisms.
 *
 * <ul>
 *   <li>{@link com.fauna.event.EventSource} - Represents an <a href="https://docs.fauna.com/fauna/current/reference/cdc/#event-source">event source</a>.</li>
 *   <li>{@link com.fauna.event.EventSourceResponse} - Encapsulates the response containing an event source token.</li>
 *   <li>{@link com.fauna.event.FaunaEvent} - Defines an <a href="https://docs.fauna.com/fauna/current/reference/cdc/#events">event</a>.</li>
 *   <li>{@link com.fauna.event.FaunaStream} - Processes events from an <a href="https://docs.fauna.com/fauna/current/reference/cdc/#event-streaming">Event Stream</a>, decoding them into {@code FaunaEvent} instances.</li>
 *   <li>{@link com.fauna.event.FeedIterator} - Enables iteration through pages of events in an <a href="https://docs.fauna.com/fauna/current/reference/cdc/#event-feeds">Event Feed</a>.</li>
 *   <li>{@link com.fauna.event.FeedOptions} - Specifies configuration options for managing Event Feed pagination and timeout.</li>
 *   <li>{@link com.fauna.event.FeedPage} - Represents a paginated events in an Event Feed, including metadata like cursor and statistics.</li>
 *   <li>{@link com.fauna.event.FeedRequest} - Constructs a request for Fauna's <a href="https://docs.fauna.com/fauna/current/reference/http/reference/core-api/#operation/feed">Event Feed HTTP API endpoint</a>.</li>
 *   <li>{@link com.fauna.event.StreamOptions} - Specified configuration options for an Event Stream, such as cursor, retry strategy, and timeout settings.</li>
 *   <li>{@link com.fauna.event.StreamRequest} - Constructs a request for Fauna's <a href="https://docs.fauna.com/fauna/current/reference/http/reference/core-api/#operation/stream">Event Stream HTTP API endpoint</a>.</li>
 * </ul>
 *
 * <p>The classes in this package are designed to support <a href="https://docs.fauna.com/fauna/current/reference/cdc/">Fauna Event Feeds and Event Streams</a>.
 */
package com.fauna.event;
