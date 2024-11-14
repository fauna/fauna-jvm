package com.fauna.exception;

/**
 * Marker interface for exceptions that indicate a retryable operation.
 * Exceptions implementing this interface suggest that the operation may be retried,
 * as the error might be transient or recoverable.
 *
 * <p>This interface allows for easy identification of retryable exceptions in Fauna.</p>
 */
public interface RetryableException {
}
