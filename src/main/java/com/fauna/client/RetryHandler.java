package com.fauna.client;

import com.fauna.exception.FaunaException;
import com.fauna.exception.RetryableException;

import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * A retry handler controls the retries for a particular request.
 */
public class RetryHandler<T> {
    private final RetryStrategy strategy;
    private final Logger logger;

    /**
     * Construct a new retry handler instance.
     * @param strategy  The retry strategy to use.
     */
    public RetryHandler(RetryStrategy strategy, Logger logger) {
        this.strategy = strategy;
        this.logger = logger;
    }

    public CompletableFuture<T> delayRequest(Supplier<CompletableFuture<T>> action, int delayMillis) {
        return CompletableFuture.supplyAsync(
                action, CompletableFuture.delayedExecutor(delayMillis, TimeUnit.MILLISECONDS)).join();
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
            boolean retryable = isRetryable(throwable);
            if (retryable && this.strategy.canRetry(retryAttempt)) {
                int delay = this.strategy.getDelayMillis(retryAttempt);
                logger.fine(MessageFormat.format("Retry attempt {0} for exception {1}", retryAttempt,
                        throwable.getClass()));
                return delayRequest(supplier, delay);
            } else {
                logger.fine(MessageFormat.format("Re-throwing {0}retryable exception: {1}",
                        retryable ? "" : "non-", throwable.getClass()));
                return rethrow(throwable);
            }
        } catch (FaunaException exc) {
            throw exc;
        } catch (Exception exc) {
            throw new FaunaException("Unexpected exception.", exc);
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
