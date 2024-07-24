package com.fauna.client;

import com.fauna.exception.FaunaException;
import com.fauna.exception.RetryableException;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A retry handler controls the retries for a particular request.
 */
public class RetryHandler<T> {
    private final RetryStrategy strategy;

    /**
     * Construct a new retry handler instance.
     * @param strategy  The retry strategy to use.
     */
    public RetryHandler(RetryStrategy strategy) {
        this.strategy = strategy;
    }

    public CompletableFuture<T> delayRequest(Supplier<CompletableFuture<T>> action, int delayMillis) {
        return CompletableFuture.supplyAsync(
                () -> action.get(), CompletableFuture.delayedExecutor(delayMillis, TimeUnit.MILLISECONDS)).join();
    }

    public static boolean isRetryable(Throwable exc) {
        return exc instanceof RetryableException || exc.getCause() instanceof RetryableException;
    }

    public CompletableFuture<T> rethrow(Throwable throwable) {
        if (throwable instanceof FaunaException) {
            throw (FaunaException) throwable;
        } else if (throwable.getCause() instanceof FaunaException) {
            throw (FaunaException) throwable.getCause();
        }
        return CompletableFuture.failedFuture(throwable);
    }

    private CompletableFuture<T> retry(Throwable throwable, int retryAttempt, Supplier<CompletableFuture<T>> supplier) {
        try {
            if (isRetryable(throwable) && this.strategy.canRetry(retryAttempt)) {
                return delayRequest(supplier, this.strategy.getDelayMillis(retryAttempt));
            } else {
                return rethrow(throwable);
            }
        } catch (FaunaException exc) {
            throw exc;
        } catch (Exception exc) {
            throw new FaunaException("oops", exc);
        }
    }

    public CompletableFuture<T> execute(Supplier<CompletableFuture<T>> action) {
        CompletableFuture<T> f = action.get();
        for(int i = 1; i <= this.strategy.getMaxRetryAttempts(); i++) {
            int finalI = i;
            f=f.thenApply(CompletableFuture::completedFuture)
                    .exceptionally(t -> retry(t, finalI, action))
                    .thenCompose(Function.identity());
        }
        return f;
    }
}
