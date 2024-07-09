package com.fauna.client;

import com.fauna.query.builder.Query;

import java.io.Serializable;

/**
 * This class represents a Fauna POST request body that can be serialized.
 */
public class FaunaRequest implements Serializable {
    Query query;

    public FaunaRequest(Query query) {
        this.query = query;
    }

    public Query getQuery() {
        return this.query;
    }
}
