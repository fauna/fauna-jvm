package com.fauna.client;

import com.fauna.exception.ThrottlingException;
import com.fauna.response.ErrorInfo;
import com.fauna.response.QueryFailure;
import com.fauna.response.QueryResponse;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.logging.Logger;

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
         *
         * @param failCount
         */
        public FakeResponder(int failCount) {
            this.failCount = failCount;
        }

        public CompletableFuture<String> getResponse() {
            responseCount += 1;
            if (responseCount > failCount) {
                return CompletableFuture.supplyAsync(
                        TestRetryHandler::timestamp);
            } else {
                return CompletableFuture.failedFuture(
                        new ThrottlingException(
                                new QueryFailure(500,
                                        QueryResponse.builder(null)
                                                .error(ErrorInfo.builder()
                                                        .build()))));
            }
        }
    }

    public static Supplier<CompletableFuture<String>> respond(
            FakeResponder responder) {
        return () -> {
            try {
                return responder.getResponse();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Test
    public void testExecute() {
        RetryHandler<String> handler =
                new RetryHandler<>(ExponentialBackoffStrategy.builder().build(),
                        Logger.getGlobal());
        handler.execute(() -> CompletableFuture.supplyAsync(
                TestRetryHandler::timestamp));

    }

    @Test
    public void testFailWithNoRetries() {
        RetryHandler<String> handler =
                new RetryHandler<>(FaunaClient.NO_RETRY_STRATEGY,
                        Logger.getGlobal());
        FakeResponder responder = new FakeResponder(1);
        CompletableFuture<String> future = handler.execute(respond(responder));
        ExecutionException exc =
                assertThrows(ExecutionException.class, future::get);
        assertInstanceOf(ThrottlingException.class, exc.getCause());
    }

    @Test
    public void testFailWithRetries() {
        RetryHandler<String> handler =
                new RetryHandler<>(new ExponentialBackoffStrategy(
                        3, 2f, 10, 20_000, 0.5f), Logger.getGlobal());
        FakeResponder responder = new FakeResponder(4);
        CompletableFuture<String> future = handler.execute(respond(responder));
        ExecutionException exc =
                assertThrows(ExecutionException.class, future::get);
        assertInstanceOf(ThrottlingException.class, exc.getCause());
    }

    @Test
    public void testSucceedWithRetries()
            throws ExecutionException, InterruptedException {
        RetryHandler<String> handler =
                new RetryHandler<>(new ExponentialBackoffStrategy(
                        3, 2f, 10, 20_000, 0.5f),
                        Logger.getGlobal());
        FakeResponder responder = new FakeResponder(1);
        String output = handler.execute(respond(responder)).get();
        assertTrue(output.length() > 10);
    }
}
