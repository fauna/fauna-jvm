package com.fauna.exception;

import com.fauna.response.QueryFailure;

/**
 * Exception representing a <a href="https://docs.fauna.com/fauna/current/reference/http/reference/errors/#rate-limits">throttling error</a> in Fauna, indicating that a query exceeded
 * <a href="https://docs.fauna.com/fauna/current/manage/plans-billing/plan-details/#throughput-limits">plan throughput limits</a>.
 * <p>
 * It implements {@link RetryableException}
 * Extends {@link ServiceException} to provide details specific to throttling errors.
 */
public class ThrottlingException extends ServiceException implements RetryableException {

    /**
     * Constructs a new {@code ThrottlingException} with the specified {@code QueryFailure} details.
     *
     * @param response The {@link QueryFailure} object containing details about the throttling error.
     */
    public ThrottlingException(final QueryFailure response) {
        super(response);
    }
}
