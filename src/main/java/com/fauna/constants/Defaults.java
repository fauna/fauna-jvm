package com.fauna.constants;

import java.time.Duration;

public class Defaults {

    public static final Duration CLIENT_TIMEOUT_BUFFER = Duration.ofSeconds(5);
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);
    public static final String LOCAL_FAUNA_SECRET = "secret";
    public static final int MAX_CONTENTION_RETRIES = 3;

}
