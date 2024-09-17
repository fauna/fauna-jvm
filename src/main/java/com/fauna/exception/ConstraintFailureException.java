package com.fauna.exception;

import com.fauna.response.ConstraintFailure;
import com.fauna.response.QueryFailure;

import java.util.List;

public class ConstraintFailureException extends ServiceException {
    public ConstraintFailureException(QueryFailure failure) {
        super(failure);
    }

    public ConstraintFailure[] getConstraintFailures() {
        return getResponse().getConstraintFailures().orElse(null);
    }
}
