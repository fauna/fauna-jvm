package com.fauna.e2e;

import com.fauna.client.FaunaClient;
import com.fauna.client.FaunaConfig;

public class LocalFauna {

    public static FaunaClient get() {
        var config = new FaunaConfig.Builder().secret("secret").endpoint("http://localhost:8443").build();
        return new FaunaClient(config);
    }
}