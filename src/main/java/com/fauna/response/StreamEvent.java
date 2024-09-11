package com.fauna.response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.FaunaTokenType;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.ClientException;
import com.fauna.response.wire.ErrorInfoWire;

import java.io.IOException;
import java.util.Optional;

import static com.fasterxml.jackson.core.JsonToken.FIELD_NAME;
import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.VALUE_STRING;
import static com.fauna.constants.ResponseFields.DATA_FIELD_NAME;
import static com.fauna.constants.ResponseFields.ERROR_FIELD_NAME;
import static com.fauna.constants.ResponseFields.LAST_SEEN_TXN_FIELD_NAME;
import static com.fauna.constants.ResponseFields.STATS_FIELD_NAME;
import static com.fauna.constants.ResponseFields.STREAM_CURSOR_FIELD_NAME;
import static com.fauna.constants.ResponseFields.STREAM_TYPE_FIELD_NAME;

public class StreamEvent<E> {
    private static final Codec<QueryStats> statsCodec = DefaultCodecProvider.SINGLETON.get(QueryStats.class);
    public enum EventType {
        STATUS, ADD, UPDATE, REMOVE, ERROR
    }

    private final EventType type;
    private final String cursor;
    private final Long txn_ts;
    private final E data;
    private final QueryStats stats;
    private final ErrorInfoWire error;


    public StreamEvent(EventType type, String cursor, Long txn_ts, E data, QueryStats stats, ErrorInfoWire error) {
        this.type = type;
        this.cursor = cursor;
        this.txn_ts = txn_ts;
        this.data = data;
        this.stats = stats;
        this.error = error;
    }

    private static StreamEvent.EventType parseEventType(JsonParser parser) throws IOException {
        if (parser.nextToken() == VALUE_STRING) {
            String typeString = parser.getText().toUpperCase();
            try {
                return StreamEvent.EventType.valueOf(typeString);
            } catch (IllegalArgumentException e) {
                throw new ClientException("Invalid event type: " + typeString, e);
            }
        } else {
            throw new ClientException("Event type should be a string, but got a " + parser.currentToken().asString());
        }
    }

    public static <E> StreamEvent<E> parse(JsonParser parser, Codec<E> dataCodec) throws IOException {
        if (parser.nextToken() == START_OBJECT) {
            String cursor = null;
            StreamEvent.EventType eventType = null;
            QueryStats stats = null;
            E data = null;
            Long txn_ts = null;
            ErrorInfoWire errorInfo = null;
            while (parser.nextToken() == FIELD_NAME) {
                String fieldName = parser.getValueAsString();
                switch (fieldName) {
                    case STREAM_CURSOR_FIELD_NAME:
                        parser.nextToken();
                        cursor = parser.getText();
                        break;
                    case DATA_FIELD_NAME:
                        UTF8FaunaParser faunaParser = new UTF8FaunaParser(parser);
                        if (faunaParser.getCurrentTokenType() == FaunaTokenType.NONE) {
                            faunaParser.read();
                        }
                        data = dataCodec.decode(faunaParser);
                        break;
                    case STREAM_TYPE_FIELD_NAME: eventType = parseEventType(parser);
                    break;
                    case STATS_FIELD_NAME:
                        stats = QueryStats.parseStats(parser);
                        break;
                    case LAST_SEEN_TXN_FIELD_NAME:
                        parser.nextToken();
                        txn_ts = parser.getValueAsLong();
                        break;
                    case ERROR_FIELD_NAME:
                        ObjectMapper mapper = new ObjectMapper();
                        errorInfo = mapper.readValue(parser, ErrorInfoWire.class);
                        break;
                }
            }
            return new StreamEvent(eventType, cursor, txn_ts, data, stats, errorInfo);
        } else {
            throw new ClientException("Invalid event starting with: " + parser.currentToken());
        }
    }

    public StreamEvent.EventType getType() {
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
        return new ErrorInfo(this.error.getCode(), this.error.getMessage());
    }


}
