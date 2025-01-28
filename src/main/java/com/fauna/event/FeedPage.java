package com.fauna.event;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fauna.client.StatsCollector;
import com.fauna.codec.Codec;
import com.fauna.exception.ClientResponseException;
import com.fauna.response.QueryResponse;
import com.fauna.response.QueryStats;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.core.JsonToken.END_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.FIELD_NAME;
import static com.fasterxml.jackson.core.JsonToken.START_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;
import static com.fauna.constants.ResponseFields.CURSOR_FIELD_NAME;
import static com.fauna.constants.ResponseFields.EVENTS_FIELD_NAME;
import static com.fauna.constants.ResponseFields.FEED_HAS_NEXT_FIELD_NAME;
import static com.fauna.constants.ResponseFields.STATS_FIELD_NAME;

/**
 * Represents a page of events from an <a href="https://docs.fauna.com/fauna/current/reference/cdc/#event-feeds">event feed</a>.
 *
 * @param <E> The type of data contained in each event.
 */
public class FeedPage<E> {
    private final List<FaunaEvent<E>> events;
    private final String cursor;
    private final boolean hasNext;
    private final QueryStats stats;
    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    /**
     * Constructs a {@code FeedPage} with the specified events, cursor, pagination flag, and statistics.
     *
     * @param events  A list of {@link FaunaEvent} objects representing the events in this page.
     * @param cursor  A {@code String} representing the cursor for pagination.
     * @param hasNext A {@code boolean} indicating if there are more pages available.
     * @param stats   A {@link QueryStats} object containing statistics for the page.
     * @throws IllegalArgumentException if {@code events} is null or {@code cursor} is blank.
     */
    public FeedPage(final List<FaunaEvent<E>> events, final String cursor,
                    final boolean hasNext, final QueryStats stats) {
        if (events == null) {
            throw new IllegalArgumentException("events cannot be null");
        }
        if (cursor == null || cursor.isBlank()) {
            throw new IllegalArgumentException("cursor cannot be blank");
        }
        this.events = events;
        this.cursor = cursor;
        this.hasNext = hasNext;
        this.stats = stats;
    }

    /**
     * Retrieves the list of events in this feed page.
     *
     * @return A {@code List} of {@link FaunaEvent} objects.
     */
    public List<FaunaEvent<E>> getEvents() {
        return events;
    }

    /**
     * Retrieves the cursor for pagination.
     *
     * @return A {@code String} representing the cursor.
     */
    public String getCursor() {
        return cursor;
    }

    /**
     * Checks if there are more pages available.
     *
     * @return {@code true} if there are more pages, {@code false} otherwise.
     */
    public boolean hasNext() {
        return hasNext;
    }

    /**
     * Retrieves the statistics for this feed page.
     *
     * @return A {@link QueryStats} object.
     */
    public QueryStats getStats() {
        return stats;
    }

    /**
     * Builder class for constructing {@code FeedPage} instances.
     *
     * @param <E> The type of data contained in each event.
     */
    public static class Builder<E> {
        private final Codec<E> elementCodec;
        private final StatsCollector statsCollector;
        private List<FaunaEvent<E>> events;
        private String cursor = "";
        private Boolean hasNext = false;
        private QueryStats stats = null;

        /**
         * Constructs a {@code Builder} with the specified codec and stats collector.
         *
         * @param elementCodec    The {@link Codec} used to decode events.
         * @param statsCollector  The {@link StatsCollector} to gather statistics for the feed.
         */
        public Builder(final Codec<E> elementCodec, final StatsCollector statsCollector) {
            this.elementCodec = elementCodec;
            this.statsCollector = statsCollector;
        }

        /**
         * Sets the list of events for the feed page.
         *
         * @param events A list of {@link FaunaEvent} objects representing the events in this page.
         * @return This {@code Builder} instance.
         */
        public Builder<E> events(final List<FaunaEvent<E>> events) {
            this.events = events;
            return this;
        }

        /**
         * Sets the cursor for pagination.
         *
         * @param cursor A {@code String} representing the cursor.
         * @return This {@code Builder} instance.
         */
        public Builder<E> cursor(final String cursor) {
            this.cursor = cursor;
            return this;
        }

        /**
         * Sets the flag indicating if there are more pages available.
         *
         * @param hasNext A {@code Boolean} indicating if there are more pages.
         * @return This {@code Builder} instance.
         */
        public Builder<E> hasNext(final Boolean hasNext) {
            this.hasNext = hasNext;
            return this;
        }

        /**
         * Sets the statistics for the feed page.
         *
         * @param stats A {@link QueryStats} object containing statistics for the page.
         * @return This {@code Builder} instance.
         */
        public Builder<E> stats(final QueryStats stats) {
            this.stats = stats;
            return this;
        }

        /**
         * Parses and sets the list of events from the provided JSON parser.
         *
         * @param parser The {@link JsonParser} to decode the events from.
         * @return This {@code Builder} instance.
         * @throws IOException if an error occurs during parsing.
         */
        public Builder<E> parseEvents(final JsonParser parser) throws IOException {
            if (parser.nextToken() == START_ARRAY) {
                List<FaunaEvent<E>> events = new ArrayList<>();
                while (parser.nextToken() != END_ARRAY) {
                    events.add(FaunaEvent.parse(parser, elementCodec));
                }
                this.events = events;
            } else {
                throw new IOException("Invalid event starting with: " + parser.currentToken());
            }
            return this;
        }

        /**
         * Builds a new {@code FeedPage} instance with the configured parameters.
         *
         * @return A new {@code FeedPage} instance.
         * @throws IllegalArgumentException if {@code events} is null or {@code cursor} is blank.
         */
        public FeedPage<E> build() {
            return new FeedPage<>(events, cursor, hasNext, stats);
        }

        /**
         * Parses and sets the appropriate field in the builder based on the JSON parser's current field.
         *
         * @param parser The {@link JsonParser} for reading the field value.
         * @return The updated {@code Builder} instance.
         * @throws IOException if an error occurs during parsing.
         */
        public Builder<E> parseField(final JsonParser parser) throws IOException {
            String fieldName = parser.getValueAsString();
            switch (fieldName) {
                case CURSOR_FIELD_NAME:
                    return cursor(parser.nextTextValue());
                case EVENTS_FIELD_NAME:
                    return parseEvents(parser);
                case STATS_FIELD_NAME:
                    QueryStats stats = QueryStats.parseStats(parser);
                    statsCollector.add(stats);
                    return stats(stats);
                case FEED_HAS_NEXT_FIELD_NAME:
                    return hasNext(parser.nextBooleanValue());
                default:
                    throw new ClientResponseException("Unknown FeedPage field: " + fieldName);
            }
        }
    }

    /**
     * Creates a new {@code Builder} for constructing a {@code FeedPage}.
     *
     * @param elementCodec   The {@link Codec} used to decode events.
     * @param statsCollector The {@link StatsCollector} to gather statistics.
     * @param <E>            The type of data contained in each event.
     * @return A new {@code Builder} instance.
     */
    public static <E> Builder<E> builder(final Codec<E> elementCodec, final StatsCollector statsCollector) {
        return new Builder<>(elementCodec, statsCollector);
    }

    /**
     * Parses an HTTP response and constructs a {@code FeedPage} instance.
     *
     * @param response       The {@link HttpResponse} containing the feed data.
     * @param elementCodec   The {@link Codec} used to decode events.
     * @param statsCollector The {@link StatsCollector} to gather statistics.
     * @param <E>            The type of data contained in each event.
     * @return The parsed {@code FeedPage}.
     * @throws ClientResponseException if an error occurs while parsing the feed response.
     */
    public static <E> FeedPage<E> parseResponse(final HttpResponse<InputStream> response,
                                                final Codec<E> elementCodec,
                                                final StatsCollector statsCollector) {
        try {
            if (response.statusCode() >= 400) {
                QueryResponse.parseResponse(response, elementCodec, statsCollector);
            }
            JsonParser parser = JSON_FACTORY.createParser(response.body());
            if (parser.nextToken() != START_OBJECT) {
                throw new ClientResponseException("Invalid event starting with: " + parser.currentToken());
            }
            Builder<E> builder = FeedPage.builder(elementCodec, statsCollector);
            while (parser.nextToken() == FIELD_NAME) {
                builder = builder.parseField(parser);
            }
            return builder.build();
        } catch (IOException e) {
            throw new ClientResponseException("Error parsing Feed response.", e);
        }
    }
}
