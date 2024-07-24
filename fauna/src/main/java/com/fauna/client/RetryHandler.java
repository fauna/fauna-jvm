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

    public CompletableFuture<T> delayRequest(Supplier<T> req) {
        return CompletableFuture.supplyAsync(req, CompletableFuture.delayedExecutor(this.getDelayMillis(), TimeUnit.MILLISECONDS));
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

    public Supplier<T> toSupplier(Callable<CompletableFuture<T>> future) {
        return () -> {
            try {
                return future.call().get();
            } catch (Exception e) {
                if (e instanceof FaunaException) {
                    throw (FaunaException) e;
                } else {
                    throw new FaunaException("Caught non-Fauna exception", e);
                }
            }
        };
    }

    public CompletableFuture<T> doCall(Callable<CompletableFuture<T>> future) {
        try {
            return future.call();
        } catch (Exception exc) {
            if (exc instanceof FaunaException) {
                throw (FaunaException) exc;
            } else {
                throw new FaunaException("Unexpected exception.", exc);
            }
        }
    }

    private CompletableFuture<T> retry(Throwable throwable, int retryAttempt, Supplier<T> supplier) {
        try {
            if (isRetryable(throwable) && this.strategy.canRetry(retryAttempt)) {
                return delayRequest(supplier);
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
            f=f.thenApply(CompletableFuture::completedFuture)
                    .exceptionally(t -> action.get())
                    .thenCompose(Function.identity());
        }
        return f;
    }
}
