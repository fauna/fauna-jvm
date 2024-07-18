package com.fauna.client;

import com.fauna.exception.FaunaException;
import com.fauna.exception.RetryableException;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class RetryHandler<T> {
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

    public CompletableFuture<HttpResponse<String>> send(HttpClient client, HttpRequest request) {
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
    public CompletableFuture<HttpResponse<String>> execute(HttpClient client, HttpRequest request) throws FaunaException, InterruptedException {
        return this.send(client, request).exceptionally(exc -> {
            if (exc instanceof RetryableException) {
                Executor delayed = CompletableFuture.delayedExecutor(this.getDelayMillis(), TimeUnit.MILLISECONDS);
                return CompletableFuture.supplyAsync(send(client, request));
            } else {
                throw new CompletionException(exc);
            }
        });
    }
     */
}
