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

public class FeedPage<E> {
    private final List<FaunaEvent<E>> events;
    private final String cursor;
    private final boolean hasNext;
    private final QueryStats stats;
    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    public FeedPage(final List<FaunaEvent<E>> events,
                    final String cursor,
                    final boolean hasNext,
                    final QueryStats stats) {
        this.events = events;
        this.cursor = cursor;
        this.hasNext = hasNext;
        this.stats = stats;
        if (events == null) {
            throw new IllegalArgumentException("events cannot be null");
        }
        if (cursor == null || cursor.isBlank()) {
            throw new IllegalArgumentException("cursor cannot be blank");
        }
    }

    public List<FaunaEvent<E>> getEvents() {
        return events;
    }

    public String getCursor() {
        return cursor;
    }

    public boolean hasNext() {
        return hasNext;
    }

    public QueryStats getStats() {
        return stats;
    }

    public static class Builder<E> {
        private final Codec<E> elementCodec;
        private final StatsCollector statsCollector;
        public List<FaunaEvent<E>> events;
        public String cursor = "";
        public Boolean hasNext = false;
        public QueryStats stats = null;

        public Builder(Codec<E> elementCodec, StatsCollector statsCollector) {
            this.elementCodec = elementCodec;
            this.statsCollector = statsCollector;
        }

        public Builder events(List<FaunaEvent<E>> events) {
            this.events = events;
            return this;
        }

        public Builder cursor(String cursor) {
            this.cursor = cursor;
            return this;
        }

        public Builder hasNext(Boolean hasNext) {
            this.hasNext = hasNext;
            return this;
        }

        public Builder stats(QueryStats stats) {
            this.stats = stats;
            return this;
        }

        public Builder<E> parseEvents(JsonParser parser) throws IOException {
            if (parser.nextToken() == START_ARRAY) {
                List<FaunaEvent<E>> events = new ArrayList<>();
                while (parser.nextToken() != END_ARRAY) {
                    events.add(FaunaEvent.parse(parser, elementCodec));
                }
                this.events = events;
            } else {
                throw new IOException("Invalid event starting with: " +
                        parser.currentToken());
            }
            return this;
        }

        public FeedPage<E> build() {
            return new FeedPage<>(events, cursor, hasNext, stats);
        }

        public Builder parseField(JsonParser parser) throws IOException {
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
                    throw new ClientResponseException(
                            "Unknown FeedPage field: " + fieldName);
            }
        }

    }

    public static <E> Builder<E> builder(Codec<E> elementCodec,
                                         StatsCollector statsCollector) {
        return new Builder<>(elementCodec, statsCollector);
    }

    public static <E> FeedPage<E> parseResponse(
            HttpResponse<InputStream> response, Codec<E> elementCodec,
            StatsCollector statsCollector) {
        try {
            // If you want to inspect the request body before it's parsed, you can uncomment this.
            // String body = new BufferedReader(new InputStreamReader(response.body()))
            // .lines().collect(Collectors.joining("\n"));
            if (response.statusCode() >= 400) {
                // It's not ideal to use the QueryResponse parser in the Feed API. But for error cases, the
                // error response that the Feed API throws is a terser (i.e. subset) version of QueryFailure, and the
                // parser gracefully handles it. A FaunaException will be thrown by parseResponse.
                QueryResponse.parseResponse(response, elementCodec,
                        statsCollector);
            }
            JsonParser parser = JSON_FACTORY.createParser(response.body());
            if (parser.nextToken() != START_OBJECT) {
                throw new ClientResponseException(
                        "Invalid event starting with: " +
                                parser.currentToken());
            }
            Builder<E> builder = FeedPage.builder(elementCodec, statsCollector);
            while (parser.nextToken() == FIELD_NAME) {
                builder.parseField(parser);
            }
            return builder.build();
        } catch (IOException e) {
            throw new ClientResponseException("Error parsing Feed response.",
                    e);
        }
    }
}

