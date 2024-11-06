package com.fauna.event;

import com.fasterxml.jackson.core.JsonParser;
import com.fauna.codec.Codec;
import com.fauna.codec.FaunaTokenType;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.ClientResponseException;
import com.fauna.response.ErrorInfo;
import com.fauna.response.QueryStats;

import java.io.IOException;
import java.util.Optional;

import static com.fasterxml.jackson.core.JsonToken.FIELD_NAME;
import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.VALUE_STRING;
import static com.fauna.constants.ResponseFields.CURSOR_FIELD_NAME;
import static com.fauna.constants.ResponseFields.DATA_FIELD_NAME;
import static com.fauna.constants.ResponseFields.ERROR_FIELD_NAME;
import static com.fauna.constants.ResponseFields.LAST_SEEN_TXN_FIELD_NAME;
import static com.fauna.constants.ResponseFields.STATS_FIELD_NAME;
import static com.fauna.constants.ResponseFields.STREAM_TYPE_FIELD_NAME;

public class FaunaEvent<E> {
    public enum EventType {
        STATUS, ADD, UPDATE, REMOVE, ERROR
    }

    private final EventType type;
    private final String cursor;
    private final Long txn_ts;
    private final E data;
    private final QueryStats stats;
    private final ErrorInfo error;


    public FaunaEvent(EventType type, String cursor, Long txn_ts, E data,
                      QueryStats stats, ErrorInfo error) {
        this.type = type;
        this.cursor = cursor;
        this.txn_ts = txn_ts;
        this.data = data;
        this.stats = stats;
        this.error = error;
    }

    public static class Builder<E> {
        private final Codec<E> dataCodec;
        String cursor = null;
        FaunaEvent.EventType eventType = null;
        QueryStats stats = null;
        E data = null;
        Long txn_ts = null;
        ErrorInfo errorInfo = null;

        public Builder(Codec<E> dataCodec) {
            this.dataCodec = dataCodec;
        }

        public Builder<E> cursor(String cursor) {
            this.cursor = cursor;
            return this;
        }

        public Builder<E> eventType(FaunaEvent.EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder<E> stats(QueryStats stats) {
            this.stats = stats;
            return this;
        }

        public Builder<E> parseData(JsonParser parser) {
            UTF8FaunaParser faunaParser = new UTF8FaunaParser(parser);
            if (faunaParser.getCurrentTokenType() == FaunaTokenType.NONE) {
                faunaParser.read();
            }
            this.data = dataCodec.decode(faunaParser);
            return this;
        }

        public Builder<E> txn_ts(Long txn_ts) {
            this.txn_ts = txn_ts;
            return this;
        }

        public Builder<E> error(ErrorInfo error) {
            this.errorInfo = error;
            // Fauna does not always return an event type, for example, if you pass an invalid cursor to the
            // stream API.
            this.eventType = EventType.ERROR;
            return this;
        }

        public FaunaEvent<E> build() {
            return new FaunaEvent<>(eventType, cursor, txn_ts, data, stats,
                    errorInfo);
        }

    }

    public static <E> Builder<E> builder(Codec<E> dataCodec) {
        return new Builder<>(dataCodec);
    }

    static <E> Builder<E> parseField(Builder<E> builder, JsonParser parser)
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
                return builder.txn_ts(parser.nextLongValue(0L));
            case ERROR_FIELD_NAME:
                return builder.error(ErrorInfo.parse(parser));
            default:
                throw new ClientResponseException(
                        "Unknown StreamEvent field: " + fieldName);
        }

    }

    private static FaunaEvent.EventType parseEventType(JsonParser parser)
            throws IOException {
        if (parser.nextToken() == VALUE_STRING) {
            String typeString = parser.getText().toUpperCase();
            try {
                return FaunaEvent.EventType.valueOf(typeString);
            } catch (IllegalArgumentException e) {
                throw new ClientResponseException(
                        "Invalid event type: " + typeString, e);
            }
        } else {
            throw new ClientResponseException(
                    "Event type should be a string, but got a " +
                            parser.currentToken().asString());
        }
    }

    public static <E> FaunaEvent<E> parse(JsonParser parser, Codec<E> dataCodec)
            throws IOException {
        if (parser.currentToken() == START_OBJECT ||
                parser.nextToken() == START_OBJECT) {
            Builder<E> builder = FaunaEvent.builder(dataCodec);
            while (parser.nextToken() == FIELD_NAME) {
                builder = parseField(builder, parser);
            }
            return builder.build();
        } else {
            throw new ClientResponseException(
                    "Invalid event starting with: " + parser.currentToken());
        }
    }

    public FaunaEvent.EventType getType() {
        return type;
    }

    public Optional<E> getData() {
        return Optional.ofNullable(data);
    }

    public Optional<Long> getTimestamp() {
        return Optional.ofNullable(txn_ts);
    }

    public String getCursor() {
        return cursor;
    }

    public QueryStats getStats() {
        return stats;
    }

    public ErrorInfo getError() {
        return this.error;
    }


}
