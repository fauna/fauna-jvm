package com.fauna.response;

import java.util.Optional;

public final class QuerySuccess<T> extends QueryResponse {

    private final T data;
    private final String staticType;

    public QuerySuccess(Builder<T> builder) {
        super(builder);
        this.data = builder.data;
        this.staticType = builder.staticType;
    }

    public T getData() {
        return data;
    }

    public Optional<String> getStaticType() {
        return Optional.ofNullable(staticType);
    }

}
