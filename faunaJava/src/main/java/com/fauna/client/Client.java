package com.fauna.client;

import com.fauna.interfaces.IDeserializer;
import com.fauna.mapping.MappingContext;
import com.fauna.response.QueryResponse;
import com.fauna.response.QuerySuccess;
import com.fauna.serialization.FaunaGenerator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Client extends BaseClient {

    private static final String QUERY_URI_PATH = "/query/1";
    long lastSeenTxn;

    private final Configuration config;
    private final IConnection connection;

    private final MappingContext defaultCtx = new MappingContext();
    private final Map<Class<?>, DataContext> dbCtxs = new HashMap<>();

    public Client(String secret) {
        this(new Configuration(secret));
    }

    public Client(Configuration config) {
        this.config = config;
        this.connection = new Connection(config);
    }

    public <DB extends DataContext> DB dataContext(Class<DB> dbClass) {
        DataContext ctx;
        synchronized (dbCtxs) {
            ctx = dbCtxs.computeIfAbsent(dbClass, k -> {
                DataContextBuilder<DB> builder = new DataContextBuilder<>();
                return builder.build(this);
            });
        }
        return dbClass.cast(ctx);
    }

    @Override
    protected <T> CompletableFuture<QuerySuccess<T>> queryAsyncInternal(Query query,
        IDeserializer<T> deserializer, MappingContext ctx, QueryOptions queryOptions) {
        if (query == null) {
            throw new IllegalArgumentException("Query cannot be null");
        }

        QueryOptions finalOptions = QueryOptions.getFinalQueryOptions(
            config.getDefaultQueryOptions(), queryOptions);
        Map<String, String> headers = getRequestHeaders(finalOptions);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        serialize(stream, query, ctx);

        return connection.doPostAsync(QUERY_URI_PATH, stream.toByteArray(), headers)
            .thenApply(httpResponse -> {
                String body = new String(httpResponse.getBody());
                QueryResponse<T> res = QueryResponse.getFromResponseBody(ctx, deserializer,
                    httpResponse.getStatusCode(), body);
                switch (res.getType()) {
                    case SUCCESS:
                        lastSeenTxn = res.getLastSeenTxn();
                        return (QuerySuccess<T>) res;
                    case FAILURE:
                        throw ExceptionFactory.fromQueryFailure(ctx, (QueryFailure) res);
                    default:
                        throw ExceptionFactory.fromRawResponse(body, httpResponse);
                }
            });
    }

    private void serialize(OutputStream stream, Query query, MappingContext ctx) {
        try (FaunaGenerator writer = new FaunaGenerator(stream)) {
            writer.writeStartObject();
            writer.writeFieldName("query");
            query.serialize(ctx, writer);
            writer.writeEndObject();
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Serialization error", e);
        }
    }

    private Map<String, String> getRequestHeaders(QueryOptions queryOptions) {
        Map<String, String> headers = new HashMap<>();
        headers.put(Headers.AUTHORIZATION, "Bearer " + config.getSecret());
        headers.put(Headers.FORMAT, "tagged");
        headers.put(Headers.DRIVER, "C#");

        if (lastSeenTxn > Long.MIN_VALUE) {
            headers.put(Headers.LAST_TXN_TS, String.valueOf(lastSeenTxn));
        }

        if (queryOptions != null) {
            if (queryOptions.getQueryTimeout() != null) {
                headers.put(Headers.QUERY_TIMEOUT_MS,
                    String.valueOf(queryOptions.getQueryTimeout().toMillis()));
            }

            if (queryOptions.getQueryTags() != null) {
                headers.put(Headers.QUERY_TAGS, encodeQueryTags(queryOptions.getQueryTags()));
            }

            if (queryOptions.getTraceParent() != null) {
                headers.put(Headers.TRACE_PARENT, queryOptions.getTraceParent());
            }

            if (queryOptions.getLinearized() != null) {
                headers.put(Headers.LINEARIZED, queryOptions.getLinearized().toString());
            }

            if (queryOptions.getTypeCheck() != null) {
                headers.put(Headers.TYPE_CHECK, queryOptions.getTypeCheck().toString());
            }
        }

        return headers;
    }

    private String encodeQueryTags(Map<String, String> tags) {
        StringBuilder encodedTags = new StringBuilder();
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            encodedTags.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
        }
        encodedTags.deleteCharAt(encodedTags.length() - 1); // Remove trailing comma
        return encodedTags.toString();
    }
}