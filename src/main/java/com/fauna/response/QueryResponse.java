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
import java.net.HttpURLConnection;
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
    QueryResponse(final Builder builder) {
        this.lastSeenTxn = builder.lastSeenTxn;
        this.summary = builder.summary;
        this.schemaVersion = builder.schemaVersion;
        this.stats = builder.stats;
        this.queryTags = builder.queryTags;
    }

    /**
     * A helper method to instantiate a new builder.
     *
     * @param codec The codec to use when parsing data.
     * @param <T>   The return type of the data.
     * @return A new Builder instance.
     */
    public static <T> Builder<T> builder(final Codec<T> codec) {
        return new Builder<>(codec);
    }

    private static <T> Builder<T> handleField(final Builder<T> builder,
                                              final JsonParser parser)
            throws IOException {
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
                throw new ClientResponseException(
                        "Unexpected field '" + fieldName + "'.");
        }
    }

    /**
     * A helper method to adapt an HTTP response into a QuerySuccess or throw
     * the appropriate FaunaException.
     *
     * @param response       The HTTP response to adapt.
     * @param codec          The codec to use when reading the HTTP response body.
     * @param statsCollector The stats collector to accumulate stats against.
     * @param <T>            The response type on success.
     * @return A QuerySuccess instance.
     * @throws FaunaException Thrown on non-200 responses.
     */
    public static <T> QuerySuccess<T> parseResponse(
            final HttpResponse<InputStream> response, final Codec<T> codec,
            final StatsCollector statsCollector) throws FaunaException {
        try {
            JsonParser parser = JSON_FACTORY.createParser(response.body());

            JsonToken firstToken = parser.nextToken();
            Builder<T> builder = QueryResponse.builder(codec);
            if (firstToken != JsonToken.START_OBJECT) {
                throw new ClientResponseException(
                        "Response must be JSON object.");
            }
            while (parser.nextToken() == JsonToken.FIELD_NAME) {
                builder = handleField(builder, parser);
            }

            if (builder.stats != null) {
                statsCollector.add(builder.stats);
            }

            int httpStatus = response.statusCode();
            if (httpStatus >= HttpURLConnection.HTTP_BAD_REQUEST) {
                QueryFailure failure = new QueryFailure(httpStatus, builder);
                ErrorHandler.handleQueryFailure(response.statusCode(), failure);
                // Fall back on ProtocolException.
                throw new ProtocolException(response.statusCode(), failure);
            }
            return builder.buildSuccess();
        } catch (IOException exc) {
            throw new ClientResponseException(
                    "Failed to handle error response.", exc,
                    response.statusCode());
        }

    }

    /**
     * Gets the last seen transaction timestamp.
     *
     * @return A long representing the last seen transaction timestamp.
     */
    public Long getLastSeenTxn() {
        return lastSeenTxn;
    }

    /**
     * Gets the schema version.
     *
     * @return A long representing the schema version.
     */
    public Long getSchemaVersion() {
        return schemaVersion;
    }

    /**
     * Gets the summary associated with the response.
     *
     * @return A string representing the summary.
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Gets the query tags associated with the response.
     *
     * @return A Map containing the query tags.
     */
    public Map<String, String> getQueryTags() {
        return queryTags;
    }

    /**
     * Gets the query stats associated with the response.
     *
     * @return A QueryStats instance.
     */
    public QueryStats getStats() {
        return stats;
    }

    public static final class Builder<T> {
        private final Codec<T> codec;
        private Long lastSeenTxn;
        private String summary;
        private Long schemaVersion;
        private QueryStats stats;
        private QueryTags queryTags;
        private String staticType;
        private ErrorInfo error;
        private T data;

        /**
         * Initializes a QueryResponse.Builder.
         *
         * @param codec The codec to use when building data.
         */
        public Builder(final Codec<T> codec) {
            this.codec = codec;
        }

        /**
         * Set the last seen transaction timestamp on the builder.
         *
         * @param lastSeenTxn The last seen transaction timestamp.
         * @return This
         */
        public Builder<T> lastSeenTxn(final Long lastSeenTxn) {
            this.lastSeenTxn = lastSeenTxn;
            return this;
        }

        /**
         * Set the schema version on the builder.
         *
         * @param schemaVersion The schema version.
         * @return This
         */
        public Builder<T> schemaVersion(final Long schemaVersion) {
            this.schemaVersion = schemaVersion;
            return this;
        }

        /**
         * Set the data on the builder by consuming the provided JsonParser with
         * the configured codec.
         *
         * @param parser The JsonParser to consume.
         * @return This
         */
        public Builder<T> data(final JsonParser parser) {
            UTF8FaunaParser faunaParser = new UTF8FaunaParser(parser);
            faunaParser.read();
            this.data = this.codec.decode(faunaParser);
            return this;
        }

        /**
         * Set the query tags on the builder.
         *
         * @param tags The query tags to set.
         * @return This
         */
        public Builder<T> queryTags(final QueryTags tags) {
            this.queryTags = tags;
            return this;
        }

        /**
         * Sets the error info on the builder.
         *
         * @param info The error info to set.
         * @return This
         */
        public Builder<T> error(final ErrorInfo info) {
            this.error = info;
            return this;
        }

        /**
         * Sets the static type on the builder.
         *
         * @param staticType The static type to set.
         * @return This
         */
        public Builder<T> staticType(final String staticType) {
            this.staticType = staticType;
            return this;
        }

        /**
         * Sets the summary on the builder.
         *
         * @param summary The summary to set.
         * @return This
         */
        public Builder<T> summary(final String summary) {
            this.summary = summary;
            return this;
        }

        /**
         * Sets the query stats on the builder.
         *
         * @param stats The query stats to set.
         * @return This
         */
        public Builder<T> stats(final QueryStats stats) {
            this.stats = stats;
            return this;
        }

        /**
         * Builds a QuerySuccess.
         *
         * @return A QuerySuccess from the current builder.
         */
        public QuerySuccess<T> buildSuccess() {
            return new QuerySuccess<>(this);
        }

        /**
         * Gets a string representing the static type.
         *
         * @return A string representing the static type.
         */
        public String getStaticType() {
            return staticType;
        }

        /**
         * Gets an ErrorInfo instance representing an error on the response.
         *
         * @return An ErrorInfo instance.
         */
        public ErrorInfo getError() {
            return error;
        }

        /**
         * Gets the parsed data from the response.
         *
         * @return The parsed data.
         */
        public T getData() {
            return data;
        }
    }
}

