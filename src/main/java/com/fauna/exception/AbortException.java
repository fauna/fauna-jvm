package com.fauna.exception;

import com.fauna.response.QueryFailure;

public class AbortException extends ServiceException {

    public AbortException(QueryFailure response) {
        super(response);
    }

    public Object getAbort() {
        if (this.getResponse().getAbort().isPresent()) {
            return this.getResponse().getAbort().get();
        } else {
            throw new RuntimeException("Abort Exception missing abort data.");
        }
    }
}
