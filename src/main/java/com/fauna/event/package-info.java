/**
 * Provides classes for managing and interacting with event streams and feeds in Fauna.
 *
 * <p>This package includes core components and utilities for handling event streaming, 
 * such as options, requests, and response handling mechanisms.
 *
 * <ul>
 *   <li>{@link com.fauna.event.EventSource} - Represents the origin or source of events in a stream.</li>
 *   <li>{@link com.fauna.event.EventSourceResponse} - Encapsulates the response containing an event source token.</li>
 *   <li>{@link com.fauna.event.FaunaEvent} - Defines an event object with metadata, allowing event data parsing and handling.</li>
 *   <li>{@link com.fauna.event.FaunaStream} - Processes incoming ByteBuffer streams, decoding them into {@code FaunaEvent} instances.</li>
 *   <li>{@link com.fauna.event.FeedIterator} - Enables iteration through pages of events in a feed.</li>
 *   <li>{@link com.fauna.event.FeedOptions} - Specifies configuration options for managing feed pagination and timeout.</li>
 *   <li>{@link com.fauna.event.FeedPage} - Represents a paginated page of events in a feed, including metadata like cursor and statistics.</li>
 *   <li>{@link com.fauna.event.FeedRequest} - Creates and serializes a request body for retrieving feed data from the Fauna API.</li>
 *   <li>{@link com.fauna.event.StreamOptions} - Configures various stream options, such as cursor, retry strategy, and timeout settings.</li>
 *   <li>{@link com.fauna.event.StreamRequest} - Constructs a request for Fauna's stream API, managing serialization and parameter handling.</li>
 * </ul>
 *
 * <p>The classes in this package are designed to support Fauna's streaming and feed services, providing 
 * flexibility in configuring and managing real-time event data for client applications.
 */
package com.fauna.event;
