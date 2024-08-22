package com.fauna.beans;

import com.fauna.annotation.FaunaField;
import com.fauna.query.builder.Query;


public class ClassWithNestedQuery {

    @FaunaField(name = "result")
    private Query query;

    public ClassWithNestedQuery(Query q) {
        query = q;
    }

    public Query getQuery() {
        return query;
    }
}
