package com.fauna.e2e;

import com.fauna.client.FaunaClient;
import com.fauna.client.FaunaConfig;

public final class LocalFauna {

    public static FaunaClient get() {
        var config = new FaunaConfig.Builder().endpoint("http://localhost:8443").secret("secret").build();
        return new FaunaClient(config);
    }

}