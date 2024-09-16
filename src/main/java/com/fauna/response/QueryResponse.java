package com.fauna.response;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fauna.codec.Codec;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.ClientResponseException;
import com.fauna.exception.ErrorHandler;
import com.fauna.exception.FaunaException;
import com.fauna.response.wire.QueryResponseWire;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.Map;

import static com.fauna.constants.ResponseFields.DATA_FIELD_NAME;
import static com.fauna.constants.ResponseFields.ERROR_FIELD_NAME;
import static com.fauna.constants.ResponseFields.LAST_SEEN_TXN_FIELD_NAME;
import static com.fauna.constants.ResponseFields.QUERY_TAGS_FIELD_NAME;
import static com.fauna.constants.ResponseFields.SCHEMA_VERSION_FIELD_NAME;
import static com.fauna.constants.ResponseFields.STATS_FIELD_NAME;
import static com.fauna.constants.ResponseFields.SUMMARY_FIELD_NAME;

public abstract class QueryResponse {

    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    private static final ObjectMapper mapper = new ObjectMapper();

    private final Long lastSeenTxn;
    private final Long schemaVersion;
    private final String summary;
    private final Map<String, String> queryTags;
    private final QueryStats stats;

    QueryResponse(QueryResponseWire response) {

        lastSeenTxn = response.getTxnTs();
        schemaVersion = response.getSchemaVersion();
        summary = response.getSummary();
        stats = response.getStats();
        queryTags = response.getQueryTags();
    }

    QueryResponse(Long lastSeenTxn, String summary, Long schemaVersion,
                  Map<String, String> queryTags, QueryStats stats) {
        this.lastSeenTxn = lastSeenTxn;
        this.summary = summary;
        this.schemaVersion = schemaVersion;
        this.stats = stats;
        this.queryTags = queryTags;
    }
    QueryResponse(Builder builder) {
        this.lastSeenTxn = builder.lastSeenTxn;
        this.summary = builder.summary;
        this.schemaVersion = builder.schemaVersion;
        this.stats = builder.stats;
        this.queryTags = builder.queryTags;
    }

    static class Builder<T> {
        final Codec<T> codec;
        Long lastSeenTxn;
        String summary;
        Long schemaVersion;
        QueryStats stats;
        QueryTags queryTags;
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

        public Builder<T> summary(String summary) {
            this.summary = summary;
            return this;
        }

        public QuerySuccess<T> buildSuccess() {
            return new QuerySuccess(this);
        }

    }

    public static <T> Builder builder(Codec<T> codec) {
        return new Builder<>(codec);
    }

    /**
     * Handle a HTTPResponse and return a QuerySuccess, or throw a FaunaException.
     * @param response          The HTTPResponse object.
     * @return                  A successful response from Fauna.
     * @throws FaunaException
     */
    public static <T>  QuerySuccess<T> handleResponse(HttpResponse<String> response, Codec<T> codec) throws FaunaException {
        String body = response.body();
        try {
            var responseInternal = mapper.readValue(body, QueryResponseWire.class);
            if (response.statusCode() >= 400) {
                ErrorHandler.handleErrorResponse(response.statusCode(), responseInternal, body);
            }
            return new QuerySuccess<>(codec, responseInternal);
        } catch (JsonProcessingException exc) { // Jackson JsonProcessingException subclasses IOException
            throw new ClientResponseException("Failed to handle error response.", exc, response.statusCode());
        }
    }

    public static <T>  QuerySuccess<T> parseResponse(HttpResponse<InputStream> response, Codec<T> codec) throws FaunaException {
        try {
                JsonParser parser = JSON_FACTORY.createParser(response.body());
                JsonToken firstToken = parser.nextToken();
                Builder<T> builder = builder(codec);
                if (firstToken != JsonToken.START_OBJECT) {
                    throw new ClientResponseException("Response must be JSON object.");
                }
                while (parser.nextToken() == JsonToken.FIELD_NAME) {
                    String fieldName = parser.getCurrentName();
                    switch (fieldName) {
                        case ERROR_FIELD_NAME:
                            builder.error(ErrorInfo.parse(parser));
                            break;
                        case DATA_FIELD_NAME:
                            builder.data(parser);
                            break;
                        case STATS_FIELD_NAME:
                            QueryStats.parseStats(parser);
                            break;
                        case QUERY_TAGS_FIELD_NAME:
                            builder.queryTags(QueryTags.parse(parser));
                            break;
                        case LAST_SEEN_TXN_FIELD_NAME:
                            builder.lastSeenTxn(parser.nextLongValue(0));
                            break;
                        case SCHEMA_VERSION_FIELD_NAME:
                            builder.schemaVersion(parser.nextLongValue(0));
                            break;
                        case SUMMARY_FIELD_NAME:
                            builder.summary(parser.nextTextValue());
                            break;
                        default:
                            throw new ClientResponseException("Unexpected field '" + fieldName + "'.");
                    }

                }

            if (response.statusCode() >= 400) {
                QueryFailure failure = new QueryFailure(response, builder);
                ErrorHandler.handleQueryFailure(response.statusCode(), failure);
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

