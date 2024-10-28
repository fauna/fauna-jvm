package com.fauna.response;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fauna.client.StatsCollector;
import com.fauna.codec.Codec;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.ClientResponseException;
import com.fauna.exception.ErrorHandler;
import com.fauna.exception.FaunaException;
import com.fauna.exception.ProtocolException;
import com.fauna.query.QueryTags;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.Map;

import static com.fauna.constants.ResponseFields.DATA_FIELD_NAME;
import static com.fauna.constants.ResponseFields.ERROR_FIELD_NAME;
import static com.fauna.constants.ResponseFields.LAST_SEEN_TXN_FIELD_NAME;
import static com.fauna.constants.ResponseFields.QUERY_TAGS_FIELD_NAME;
import static com.fauna.constants.ResponseFields.SCHEMA_VERSION_FIELD_NAME;
import static com.fauna.constants.ResponseFields.STATIC_TYPE_FIELD_NAME;
import static com.fauna.constants.ResponseFields.STATS_FIELD_NAME;
import static com.fauna.constants.ResponseFields.SUMMARY_FIELD_NAME;

public abstract class QueryResponse {

    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    private final Long lastSeenTxn;
    private final Long schemaVersion;
    private final String summary;
    private final Map<String, String> queryTags;
    private final QueryStats stats;

    @SuppressWarnings("rawtypes")
    QueryResponse(Builder builder) {
        this.lastSeenTxn = builder.lastSeenTxn;
        this.summary = builder.summary;
        this.schemaVersion = builder.schemaVersion;
        this.stats = builder.stats;
        this.queryTags = builder.queryTags;
    }

    public static class Builder<T> {
        final Codec<T> codec;
        Long lastSeenTxn;
        String summary;
        Long schemaVersion;
        QueryStats stats;
        QueryTags queryTags;
        String staticType;
        ErrorInfo error;
        T data;

        public Builder(Codec<T> codec) {
            this.codec = codec;
        }

        public Builder<T> lastSeenTxn(Long lastSeenTxn) {
            this.lastSeenTxn = lastSeenTxn;
            return this;
        }

        public Builder<T> schemaVersion(Long schemaVersion) {
            this.schemaVersion = schemaVersion;
            return this;
        }

        public Builder<T> data(JsonParser parser) {
            UTF8FaunaParser faunaParser = new UTF8FaunaParser(parser);
            faunaParser.read();
            this.data = this.codec.decode(faunaParser);
            return this;
        }

        public Builder<T> queryTags(QueryTags tags) {
            this.queryTags = tags;
            return this;
        }

        public Builder<T> error(ErrorInfo info) {
            this.error = info;
            return this;
        }

        public Builder<T> staticType(String staticType) {
            this.staticType = staticType;
            return this;
        }

        public Builder<T> summary(String summary) {
            this.summary = summary;
            return this;
        }

        public Builder<T> stats(QueryStats stats) {
            this.stats = stats;
            return this;
        }

        public QuerySuccess<T> buildSuccess() {
            return new QuerySuccess<>(this);
        }

    }

    public static <T> Builder<T> builder(Codec<T> codec) {
        return new Builder<>(codec);
    }

    private static <T> Builder<T> handleField(Builder<T> builder, JsonParser parser) throws IOException {
        String fieldName = parser.getCurrentName();
        switch (fieldName) {
            case ERROR_FIELD_NAME:
                return builder.error(ErrorInfo.parse(parser));
            case DATA_FIELD_NAME:
                return builder.data(parser);
            case STATS_FIELD_NAME:
                return builder.stats(QueryStats.parseStats(parser));
            case QUERY_TAGS_FIELD_NAME:
                return builder.queryTags(QueryTags.parse(parser));
            case LAST_SEEN_TXN_FIELD_NAME:
                return builder.lastSeenTxn(parser.nextLongValue(0));
            case SCHEMA_VERSION_FIELD_NAME:
                return builder.schemaVersion(parser.nextLongValue(0));
            case STATIC_TYPE_FIELD_NAME:
                return builder.staticType(parser.nextTextValue());
            case SUMMARY_FIELD_NAME:
                return builder.summary(parser.nextTextValue());
            default:
                throw new ClientResponseException("Unexpected field '" + fieldName + "'.");
        }
    }

    public static <T>  QuerySuccess<T> parseResponse(HttpResponse<InputStream> response, Codec<T> codec, StatsCollector statsCollector) throws FaunaException {
        try {
            JsonParser parser = JSON_FACTORY.createParser(response.body());

            JsonToken firstToken = parser.nextToken();
            Builder<T> builder = QueryResponse.builder(codec);
            if (firstToken != JsonToken.START_OBJECT) {
                throw new ClientResponseException("Response must be JSON object.");
            }
            while (parser.nextToken() == JsonToken.FIELD_NAME) {
                builder = handleField(builder, parser);
            }

            if (statsCollector != null && builder.stats != null) {
                statsCollector.add(builder.stats);
            }

            int httpStatus = response.statusCode();
            if (httpStatus >= 400) {
                QueryFailure failure = new QueryFailure(httpStatus, builder);
                ErrorHandler.handleQueryFailure(response.statusCode(), failure);
                // Fall back on ProtocolException.
                throw new ProtocolException(response.statusCode(), failure);
            }
            return builder.buildSuccess();
        } catch (IOException exc) {
            throw new ClientResponseException("Failed to handle error response.", exc, response.statusCode());
        }

    }


    public Long getLastSeenTxn() {
        return lastSeenTxn;
    }

    public Long getSchemaVersion() {
        return schemaVersion;
    }

    public String getSummary() {
        return summary;
    }

    public Map<String, String> getQueryTags() {
        return queryTags;
    }

    public QueryStats getStats() {
        return stats;
    }
}

