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
 *
 * @param <T> The return type for a successful response.
 */
public final class RetryHandler<T> {
    private final RetryStrategy strategy;
    private final Logger logger;

    /**
     * Constructs a new retry handler instance.
     *
     * @param strategy The retry strategy to use.
     * @param logger   The logger used to log retry details.
     */
    public RetryHandler(final RetryStrategy strategy, final Logger logger) {
        this.strategy = strategy;
        this.logger = logger;
    }

    /**
     * Delays the request execution by a specified delay in milliseconds.
     *
     * @param action      The action to be executed.
     * @param delayMillis The delay in milliseconds before executing the action.
     * @return A CompletableFuture representing the result of the action.
     */
    public CompletableFuture<T> delayRequest(
            final Supplier<CompletableFuture<T>> action, final int delayMillis) {
        return CompletableFuture.supplyAsync(
                action, CompletableFuture.delayedExecutor(delayMillis,
                        TimeUnit.MILLISECONDS)).join();
    }

    /**
     * Checks if an exception is retryable.
     *
     * @param exc The exception to check.
     * @return True if the exception or its cause is retryable.
     */
    public static boolean isRetryable(final Throwable exc) {
        return exc instanceof RetryableException ||
                exc.getCause() instanceof RetryableException;
    }

    /**
     * Rethrows a throwable as a FaunaException.
     *
     * @param throwable The throwable to be rethrown.
     * @return A failed CompletableFuture containing the throwable.
     */
    public CompletableFuture<T> rethrow(final Throwable throwable) {
        if (throwable instanceof FaunaException) {
            throw (FaunaException) throwable;
        } else if (throwable.getCause() instanceof FaunaException) {
            throw (FaunaException) throwable.getCause();
        }
        return CompletableFuture.failedFuture(throwable);
    }

    /**
     * Retries the action based on the retry strategy.
     *
     * @param throwable   The throwable that caused the failure.
     * @param retryAttempt The current retry attempt number.
     * @param supplier    The action to retry.
     * @return A CompletableFuture representing the result of the retried action.
     */
    private CompletableFuture<T> retry(final Throwable throwable, final int retryAttempt,
                                       final Supplier<CompletableFuture<T>> supplier) {
        try {
            boolean retryable = isRetryable(throwable);
            if (retryable && this.strategy.canRetry(retryAttempt)) {
                int delay = this.strategy.getDelayMillis(retryAttempt);
                logger.fine(MessageFormat.format(
                        "Retry attempt {0} for exception {1}", retryAttempt,
                        throwable.getClass()));
                return delayRequest(supplier, delay);
            } else {
                logger.fine(MessageFormat.format(
                        "Re-throwing {0}retryable exception: {1}",
                        retryable ? "" : "non-", throwable.getClass()));
                return rethrow(throwable);
            }
        } catch (FaunaException exc) {
            throw exc;
        } catch (Exception exc) {
            throw new FaunaException("Unexpected exception.", exc);
        }
    }

    /**
     * Executes an action with retry logic based on the retry strategy.
     *
     * @param action The action to execute.
     * @return A CompletableFuture representing the result of the action.
     */
    public CompletableFuture<T> execute(final Supplier<CompletableFuture<T>> action) {
        CompletableFuture<T> f = action.get();
        for (int i = 1; i <= this.strategy.getMaxRetryAttempts(); i++) {
            final int finalI = i;
            f = f.thenApply(CompletableFuture::completedFuture)
                    .exceptionally(t -> retry(t, finalI, action))
                    .thenCompose(Function.identity());
        }
        return f;
    }
}
