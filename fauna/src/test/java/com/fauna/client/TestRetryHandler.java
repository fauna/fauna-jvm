package com.fauna.client;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRetryHandler {

    public static String timestamp() {
        return ZonedDateTime.now().format(DateTimeFormatter.ISO_TIME);
    }

    public static CompletableFuture<String> justDelay(String input, int seconds) {
        Executor delayed = CompletableFuture.delayedExecutor(seconds, TimeUnit.SECONDS);
        return CompletableFuture.supplyAsync(() -> input, delayed);
    }

    public static CompletableFuture<String> sendAsync(String input) {
        return CompletableFuture.supplyAsync(() -> MessageFormat.format("{0} -> done ({1})",
                input, timestamp()));

    }

    @Test
    public void testDefaultHandler() {
        RetryHandler handler = new RetryHandler(HttpClient.newBuilder().build(),
                HttpRequest.newBuilder().uri(URI.create("http://foo")).build(),
                ExponentialBackoffStrategy.builder().build());
        assertEquals(750, handler.getDelayMillis(), 250);
        assertEquals(1500, handler.getDelayMillis(), 500);
    }

    @Test
    public void testDelay() throws ExecutionException, InterruptedException {
        this.sendAsync(timestamp()).thenCompose(TestRetryHandler::sendAsync);
    }
}
