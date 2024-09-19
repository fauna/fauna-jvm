package com.fauna.exception;

import com.fauna.response.QueryFailure;

public class ContendedTransactionException extends ServiceException {
    public ContendedTransactionException(QueryFailure response) {
        super(response);
    }
}
