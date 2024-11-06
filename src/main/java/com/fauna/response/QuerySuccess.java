package com.fauna.response;

import java.util.Optional;

public final class QuerySuccess<T> extends QueryResponse {

    private final T data;
    private final String staticType;

    public QuerySuccess(final Builder<T> builder) {
        super(builder);
        this.data = builder.getData();
        this.staticType = builder.getStaticType();
    }

    public T getData() {
        return data;
    }

    public Optional<String> getStaticType() {
        return Optional.ofNullable(staticType);
    }

}
