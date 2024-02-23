package com.fauna.client;


import com.fauna.interfaces.IDeserializer;
import com.fauna.mapping.MappingContext;
import com.fauna.response.QuerySuccess;
import com.fauna.serialization.Deserializer;
import java.util.concurrent.CompletableFuture;


public abstract class BaseClient implements IClient {

    private final MappingContext MappingCtx;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    protected BaseClient(MappingContext mappingCtx) {
        this.MappingCtx = mappingCtx;
    }

    protected abstract CompletableFuture<QuerySuccess<Object>> QueryAsyncInternal(
        Query query,
        IDeserializer<Object> deserializer,
        MappingContext ctx,
        QueryOptions queryOptions
    );

    @Override
    public <T> CompletableFuture<QuerySuccess<T>> QueryAsync(
        Query query,
        QueryOptions queryOptions
    ) {
        return QueryAsync(query, Deserializer.generate(MappingCtx), queryOptions);
    }

    @Override
    public CompletableFuture<QuerySuccess<Object>> QueryAsync(
        Query query,
        QueryOptions queryOptions
    ) {
        return QueryAsync(query, Deserializer.DYNAMIC, queryOptions);
    }

    @Override
    public <T> CompletableFuture<QuerySuccess<T>> QueryAsync(
        Query query,
        IDeserializer<T> deserializer,
        QueryOptions queryOptions
    ) {
        CompletableFuture<QuerySuccess<T>> future = new CompletableFuture<>();

        CompletableFuture.supplyAsync(
                () -> QueryAsyncInternal(query, deserializer, MappingCtx, queryOptions), executor)
            .thenAccept(result -> {
                // Check if the task was canceled
                if (future.isCancelled()) {
                    // If canceled, complete the future exceptionally with CancellationException
                    future.completeExceptionally(new CancellationException("Task was canceled"));
                } else {
                    // Otherwise, complete the future with the result
                    future.complete((QuerySuccess<T>) result);
                }
            })
            .exceptionally(ex -> {
                // If an exception occurred, complete the future exceptionally
                future.completeExceptionally(ex);
                return null;
            });

        return future;
    }


}
