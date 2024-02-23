package com.fauna.client;


import com.fauna.interfaces.IDeserializer;
import com.fauna.response.QuerySuccess;
import java.util.concurrent.CompletableFuture;

public interface IClient {

    <T> CompletableFuture<QuerySuccess<T>> queryAsync(
        Query query,
        QueryOptions queryOptions
    );

    <T> CompletableFuture<QuerySuccess<T>> queryAsync(
        Query query,
        IDeserializer<T> deserializer,
        QueryOptions queryOptions
    );


}