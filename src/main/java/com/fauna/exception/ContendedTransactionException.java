package com.fauna.exception;

import com.fauna.response.QueryFailure;

/**
 * An exception indicating that too much <a href="https://docs.fauna.com/fauna/current/learn/transactions/contention/">transaction contention</a> occurred while executing a query.
 * <p>
 * This exception is thrown when a transaction cannot proceed due to conflicts or contention
 * with other concurrent transactions.
 * <p>
 * Extends {@link ServiceException} to provide detailed information about the failed query.
 *
 * @see ServiceException
 * @see QueryFailure
 */
public class ContendedTransactionException extends ServiceException {

    /**
     * Constructs a new {@code ContendedTransactionException} with the specified {@code QueryFailure} response.
     *
     * @param response The {@code QueryFailure} object containing details about the failed query.
     */
    public ContendedTransactionException(final QueryFailure response) {
        super(response);
    }
}
