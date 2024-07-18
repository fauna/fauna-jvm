package com.fauna.client;

import com.fauna.response.QueryResponse;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class RetryHandler {
    private final RetryStrategy strategy;
    private final long startTimeMillis;
    private int requestCount = 0;
    private final HttpClient client;
    private final HttpRequest request;


    public RetryHandler(HttpClient client, HttpRequest request, RetryStrategy strategy) {
        this.strategy = strategy;
        this.startTimeMillis = System.currentTimeMillis();
        this.client = client;
        this.request = request;
    }

    public int getDelayMillis() {
        this.requestCount += 1;
        return this.strategy.getDelayMillis(startTimeMillis, requestCount);
    }

    public CompletableFuture<Integer> delayRequest() {
        Integer delayed = this.getDelayMillis();
        return CompletableFuture.supplyAsync(() -> delayed, CompletableFuture.delayedExecutor(delayed, TimeUnit.MILLISECONDS));
    }

    public static CompletableFuture<QueryResponse> sendAsync(HttpClient client, HttpRequest request) {
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(QueryResponse::handleResponse);
    }

    private CompletableFuture<QueryResponse> retry(Throwable first, int retry) {
        if(retry >= 4) return CompletableFuture.failedFuture(first);
        return this.delayRequest().thenCompose(foo -> this.sendAsync(client, request))
                .thenApply(CompletableFuture::completedFuture)
                .exceptionally(t -> { first.addSuppressed(t); return retry(first, retry+1); })
                .thenCompose(Function.identity());
    }

    public CompletableFuture<QueryResponse> execute() {
        return this.sendAsync(client, request)
                .thenApply(CompletableFuture::completedFuture)
                .exceptionally(t -> retry(t, 0))
                .thenCompose(Function.identity());
    }

}
