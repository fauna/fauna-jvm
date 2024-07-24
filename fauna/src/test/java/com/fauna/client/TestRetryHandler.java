package com.fauna.client;

import com.fauna.exception.ThrottlingException;
import com.fauna.response.QueryFailure;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestRetryHandler {


    public static String timestamp() {
        return ZonedDateTime.now().format(DateTimeFormatter.ISO_TIME);
    }

    public static class FakeResponder {
        private int responseCount = 0;
        private final int failCount;

        /**
         * Responds with n throttling exceptions before responding with a success.
         * @param failCount
         */
        public FakeResponder(int failCount) {
            this.failCount = failCount;
        }

        public CompletableFuture<String> getResponse() {
            responseCount += 1;
            if (responseCount > failCount) {
                return CompletableFuture.supplyAsync(TestRetryHandler::timestamp);
            } else {
                return CompletableFuture.failedFuture(new ThrottlingException(QueryFailure.fallback()));
            }
        }
    }

    public static Supplier<CompletableFuture<String>> respond(FakeResponder responder) {
        return () -> responder.getResponse();
    }

    @Test
    public void testExecute() {
        RetryHandler<String> handler = new RetryHandler<>(ExponentialBackoffStrategy.builder().build());
        handler.execute(() -> CompletableFuture.supplyAsync(TestRetryHandler::timestamp));

    }

    @Test
    public void testFailWithNoRetries() {
        RetryHandler<String> handler  = new RetryHandler<>(ExponentialBackoffStrategy.NO_RETRIES);
        FakeResponder responder = new FakeResponder(1);
        CompletableFuture<String> future = handler.execute(respond(responder));
        ExecutionException exc = assertThrows(ExecutionException.class, future::get);
        assertInstanceOf(ThrottlingException.class, exc.getCause());
    }

    @Test
    public void testFailWithRetries() {
        RetryHandler<String> handler  = new RetryHandler<>(new ExponentialBackoffStrategy(
                3, 2f, 10, 20_000, 0.5f));
        FakeResponder responder = new FakeResponder(4);
        CompletableFuture<String> future = handler.execute(respond(responder));
        ExecutionException exc = assertThrows(ExecutionException.class, future::get);
        assertInstanceOf(ThrottlingException.class, exc.getCause());
    }

    @Test
    public void testSucceedWithRetries() throws ExecutionException, InterruptedException {
        RetryHandler<String> handler  = new RetryHandler<>(new ExponentialBackoffStrategy(
                3, 2f, 10, 20_000, 0.5f));
        FakeResponder responder = new FakeResponder(1);
        String output = handler.execute(respond(responder)).get();
        assertTrue(output.length() > 10);
    }
}
