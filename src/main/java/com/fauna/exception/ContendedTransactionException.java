package com.fauna.exception;

import com.fauna.response.QueryFailure;

public class ContendedTransactionException extends ServiceException {
    public static final String ERROR_CODE = "contended_transaction";
    public ContendedTransactionException(QueryFailure response) {
        super(response);
    }
}
