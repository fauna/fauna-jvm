package com.fauna.client;

public interface RetryStrategy {

    int getDelayMillis(long initialRequestMillis, int requestCount);

}
