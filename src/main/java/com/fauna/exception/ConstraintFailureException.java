package com.fauna.exception;

import com.fauna.response.ConstraintFailure;
import com.fauna.response.QueryFailure;

/**
 * Exception representing a <a href="https://docs.fauna.com/fauna/current/reference/http/reference/errors/#constraints">constraint failure</a> in a Fauna query.
 * <p>
 * This exception is typically thrown when a query violates a collection's <a href="https://docs.fauna.com/fauna/current/reference/fsl/check/">check</a> 
 * or <a href="https://docs.fauna.com/fauna/current/reference/fsl/unique/">unique constraints</a>.
 * Extends {@link ServiceException} and provides access to details about the constraint failures.
 */
public class ConstraintFailureException extends ServiceException {

    /**
     * Constructs a new {@code ConstraintFailureException} with the specified {@code QueryFailure}.
     *
     * @param failure The {@code QueryFailure} object containing details about the constraint failure.
     */
    public ConstraintFailureException(final QueryFailure failure) {
        super(failure);
    }

    /**
     * Retrieves an array of {@link ConstraintFailure} objects representing the individual constraint failures.
     *
     * @return An array of {@code ConstraintFailure} objects, or {@code null} if no constraint failures are present.
     */
    public ConstraintFailure[] getConstraintFailures() {
        return getResponse().getConstraintFailures().orElse(null);
    }
}
