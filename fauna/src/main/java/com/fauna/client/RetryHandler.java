package com.fauna.client;

import com.fauna.exception.RetryableException;
import com.fauna.response.QueryResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * A retry handler controls the retries for a particular request.
 */
public class RetryHandler {
    private final RetryStrategy strategy;
    private int requestCount = 0;


    /**
     * Construct a new retry handler instance.
     * @param strategy  The retry strategy to use.
     */
    public RetryHandler(RetryStrategy strategy) {
        this.strategy = strategy;
    }

    public int getDelayMillis() {
        this.requestCount += 1;
        return this.strategy.getDelayMillis(requestCount);
    }

    public CompletableFuture<Void> delayRequest() {
        return CompletableFuture.supplyAsync(() -> null, CompletableFuture.delayedExecutor(this.getDelayMillis(), TimeUnit.MILLISECONDS));
    }


    private CompletableFuture<QueryResponse> retry(Throwable first, int retryAttempt,
                                                   CompletableFuture<QueryResponse> send) {
        if (first instanceof RetryableException && this.strategy.canRetry(retryAttempt)) {
            return this.delayRequest().thenCompose(request -> send)
                    .thenApply(CompletableFuture::completedFuture)
                    .exceptionally(t -> retry(first, retryAttempt+1, send))
                    .thenCompose(Function.identity());
        } else {
            return CompletableFuture.failedFuture(first);
        }
    }

    public CompletableFuture<QueryResponse> execute(CompletableFuture<QueryResponse> send) {
        return send.thenApply(CompletableFuture::completedFuture)
                .exceptionally(t -> retry(t, 0, send))
                .thenCompose(Function.identity());
    }

}
