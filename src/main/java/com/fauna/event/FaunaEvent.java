package com.fauna.event;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fauna.codec.Codec;
import com.fauna.codec.FaunaTokenType;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.ClientResponseException;
import com.fauna.response.ErrorInfo;
import com.fauna.response.QueryStats;

import java.io.IOException;
import java.util.Optional;

import static com.fauna.constants.ResponseFields.CURSOR_FIELD_NAME;
import static com.fauna.constants.ResponseFields.DATA_FIELD_NAME;
import static com.fauna.constants.ResponseFields.ERROR_FIELD_NAME;
import static com.fauna.constants.ResponseFields.LAST_SEEN_TXN_FIELD_NAME;
import static com.fauna.constants.ResponseFields.STATS_FIELD_NAME;
import static com.fauna.constants.ResponseFields.STREAM_TYPE_FIELD_NAME;

/**
 * Represents an event emitted in an <a href="https://docs.fauna.com/fauna/current/reference/cdc/#event-feeds">Event Feed</a> or <a href="https://docs.fauna.com/fauna/current/reference/cdc/#event-streaming">Event Stream</a>.
 * @param <E> The type of data contained in the event.
 */
public final class FaunaEvent<E> {

    /**
     * Enum representing possible event types from a Fauna event source.
     */
    public enum EventType {
        STATUS, ADD, UPDATE, REMOVE, ERROR
    }

    private final EventType type;
    private final String cursor;
    private final Long txnTs;
    private final E data;
    private final QueryStats stats;
    private final ErrorInfo error;

    /**
     * Constructs a new {@code FaunaEvent} with the specified properties.
     *
     * @param type    The type of the event.
     * @param cursor  The cursor for the event.
     * @param txnTs   The transaction timestamp for the document change that triggered the event.
     * @param data    The data for the document that triggered the event.
     * @param stats   The <a href="https://docs.fauna.com/fauna/current/reference/http/reference/query-stats/#event-stats">event stats</a>.
     * @param error   The error information for the event, if any.
     */
    public FaunaEvent(final EventType type, final String cursor, final Long txnTs,
                      final E data, final QueryStats stats, final ErrorInfo error) {
        this.type = type;
        this.cursor = cursor;
        this.txnTs = txnTs;
        this.data = data;
        this.stats = stats;
        this.error = error;
    }

    /**
     * Retrieves the type of this event.
     *
     * @return The {@link EventType} of this event.
     */
    public EventType getType() {
        return type;
    }

    /**
     * Retrieves the Fauna document data associated with this event.
     *
     * @return An {@link Optional} containing the event data, or empty if no data is available.
     */
    public Optional<E> getData() {
        return Optional.ofNullable(data);
    }

    /**
     * Retrieves the transaction timestamp for the document change that triggered the event.
     *
     * @return An {@link Optional} containing the transaction timestamp, or empty if not present.
     */
    public Optional<Long> getTimestamp() {
        return Optional.ofNullable(txnTs);
    }

    /**
     * Retrieves the cursor for this event.
     *
     * @return A {@code String} representing the cursor.
     */
    public String getCursor() {
        return cursor;
    }

    /**
     * Retrieves stats associated with this event.
     *
     * @return A {@link QueryStats} object representing the statistics.
     */
    public QueryStats getStats() {
        return stats;
    }

    /**
     * Retrieves the error information for this event, if any.
     *
     * @return An {@link ErrorInfo} object containing error details, or {@code null} if no error is present.
     */
    public ErrorInfo getError() {
        return this.error;
    }

    /**
     * Builder class for constructing a {@code FaunaEvent} instance.
     *
     * @param <E> The type of data contained in the event.
     */
    public static final class Builder<E> {
        private final Codec<E> dataCodec;
        private String cursor = null;
        private FaunaEvent.EventType eventType = null;
        private QueryStats stats = null;
        private E data = null;
        private Long txnTs = null;
        private ErrorInfo errorInfo = null;

        /**
         * Constructs a {@code Builder} for building a {@code FaunaEvent}.
         *
         * @param dataCodec The {@link Codec} used to decode event data.
         */
        public Builder(final Codec<E> dataCodec) {
            this.dataCodec = dataCodec;
        }

        /**
         * Sets the cursor for the event.
         *
         * @param cursor The cursor to set.
         * @return This {@code Builder} instance.
         */
        public Builder<E> cursor(final String cursor) {
            this.cursor = cursor;
            return this;
        }

        /**
         * Sets the event type.
         *
         * @param eventType The {@link EventType} of the event.
         * @return This {@code Builder} instance.
         */
        public Builder<E> eventType(final FaunaEvent.EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        /**
         * Sets the query statistics for the event.
         *
         * @param stats The {@link QueryStats} to set.
         * @return This {@code Builder} instance.
         */
        public Builder<E> stats(final QueryStats stats) {
            this.stats = stats;
            return this;
        }

        /**
         * Parses and sets the event data from the given JSON parser.
         *
         * @param parser The {@link JsonParser} to decode the data from.
         * @return This {@code Builder} instance.
         */
        public Builder<E> parseData(final JsonParser parser) {
            UTF8FaunaParser faunaParser = new UTF8FaunaParser(parser);
            if (faunaParser.getCurrentTokenType() == FaunaTokenType.NONE) {
                faunaParser.read();
            }
            this.data = dataCodec.decode(faunaParser);
            return this;
        }

        /**
         * Sets the transaction timestamp for the event.
         *
         * @param txnTs The transaction timestamp to set.
         * @return This {@code Builder} instance.
         */
        public Builder<E> txnTs(final Long txnTs) {
            this.txnTs = txnTs;
            return this;
        }

        /**
         * Sets the error information for the event.
         *
         * @param error The {@link ErrorInfo} containing error details.
         * @return This {@code Builder} instance.
         */
        public Builder<E> error(final ErrorInfo error) {
            this.errorInfo = error;
            this.eventType = EventType.ERROR;
            return this;
        }

        /**
         * Builds and returns a {@code FaunaEvent} instance.
         *
         * @return A new {@code FaunaEvent} instance.
         */
        public FaunaEvent<E> build() {
            return new FaunaEvent<>(eventType, cursor, txnTs, data, stats, errorInfo);
        }
    }

    /**
     * Creates a new {@code Builder} for constructing a {@code FaunaEvent}.
     *
     * @param dataCodec The {@link Codec} used to decode event data.
     * @param <E>       The type of data contained in the event.
     * @return A new {@code Builder} instance.
     */
    public static <E> Builder<E> builder(final Codec<E> dataCodec) {
        return new Builder<>(dataCodec);
    }

    /**
     * Parses and sets the appropriate field in the builder based on the JSON parser's current field.
     *
     * @param builder The {@code Builder} being populated.
     * @param parser  The {@link JsonParser} for reading the field value.
     * @param <E>     The type of data contained in the event.
     * @return The updated {@code Builder} instance.
     * @throws IOException If an error occurs while parsing.
     */
    private static <E> Builder<E> parseField(final Builder<E> builder, final JsonParser parser)
            throws IOException {
        String fieldName = parser.getValueAsString();
        switch (fieldName) {
            case CURSOR_FIELD_NAME:
                return builder.cursor(parser.nextTextValue());
            case DATA_FIELD_NAME:
                return builder.parseData(parser);
            case STREAM_TYPE_FIELD_NAME:
                return builder.eventType(parseEventType(parser));
            case STATS_FIELD_NAME:
                return builder.stats(QueryStats.parseStats(parser));
            case LAST_SEEN_TXN_FIELD_NAME:
                return builder.txnTs(parser.nextLongValue(0L));
            case ERROR_FIELD_NAME:
                return builder.error(ErrorInfo.parse(parser));
            default:
                throw new ClientResponseException("Unknown FaunaEvent field: " + fieldName);
        }
    }

    /**
     * Parses the event type from the JSON parser.
     *
     * @param parser The {@link JsonParser} positioned at the event type field.
     * @return The parsed {@link EventType}.
     * @throws IOException If an error occurs while parsing.
     */
    private static FaunaEvent.EventType parseEventType(final JsonParser parser)
            throws IOException {
        if (parser.nextToken() == JsonToken.VALUE_STRING) {
            String typeString = parser.getText().toUpperCase();
            try {
                return FaunaEvent.EventType.valueOf(typeString);
            } catch (IllegalArgumentException e) {
                throw new ClientResponseException("Invalid event type: " + typeString, e);
            }
        } else {
            throw new ClientResponseException("Event type should be a string, but got a "
                    + parser.currentToken().asString());
        }
    }

    /**
     * Parses a {@code FaunaEvent} from the JSON parser using the specified codec.
     *
     * @param parser    The {@link JsonParser} positioned at the start of the event.
     * @param dataCodec The {@link Codec} used to decode event data.
     * @param <E>       The type of data contained in the event.
     * @return The parsed {@code FaunaEvent}.
     * @throws IOException If an error occurs while parsing.
     */
    public static <E> FaunaEvent<E> parse(final JsonParser parser, final Codec<E> dataCodec)
            throws IOException {
        if (parser.currentToken() == JsonToken.START_OBJECT || parser.nextToken() == JsonToken.START_OBJECT) {
            Builder<E> builder = FaunaEvent.builder(dataCodec);
            while (parser.nextToken() == JsonToken.FIELD_NAME) {
                builder = parseField(builder, parser);
            }
            return builder.build();
        } else {
            throw new ClientResponseException("Invalid event starting with: " + parser.currentToken());
        }
    }
}
