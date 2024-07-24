package com.fauna.exception;

import com.fauna.response.QueryFailure;

public class ConstraintFailureException extends ServiceException {
    public ConstraintFailureException(QueryFailure failure) {
        super(failure);
    }
}
