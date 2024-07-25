package com.fauna.e2e;

import com.fauna.client.FaunaClient;
import com.fauna.client.FaunaConfig;
import com.fauna.query.builder.Query;
import com.fauna.response.QueryFailure;
import com.fauna.response.QuerySuccess;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

public class LocalFauna {

    private final FaunaClient client;

    LocalFauna(FaunaClient client) {
        this.client = client;
    }

    public static LocalFauna get() {
        var config = new FaunaConfig.Builder().secret("secret").endpoint("http://localhost:8443").build();
        return new LocalFauna(new FaunaClient(config));
    }

    public <T> CompletableFuture<T> query(Query query) {
        return client.asyncQuery(query).thenApply(response -> {
            if (response instanceof QueryFailure qf) {
                throw new IllegalStateException("Query failed with: " + qf.getMessage());
            }
            @SuppressWarnings("unchecked")
            var success = (QuerySuccess<T>) response;
            return success.getData();
        });
    }

    public <T> CompletableFuture<T> query(Query query, Type t) {
        return client.asyncQuery(query, t).thenApply(response -> {
            if (response instanceof QueryFailure qf) {
                throw new IllegalStateException("Query failed with: " + qf.getMessage());
            }
            @SuppressWarnings("unchecked")
            var success = (QuerySuccess<T>) response;
            return success.getData();
        });
    }
}