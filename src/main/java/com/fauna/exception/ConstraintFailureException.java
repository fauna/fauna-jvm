package com.fauna.exception;

import com.fauna.response.ConstraintFailure;
import com.fauna.response.QueryFailure;

public class ConstraintFailureException extends ServiceException {
    public ConstraintFailureException(QueryFailure failure) {
        super(failure);
    }

    public ConstraintFailure[] getConstraintFailures() {
        return this.getResponse().getConstraintFailures().orElseThrow();
    }

}
