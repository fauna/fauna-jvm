package com.fauna.constants;

import java.time.Duration;

/**
 * Defines default constants used throughout the Fauna client.
 *
 * <p>The {@code Defaults} class includes constants for configuration settings, such as timeouts,
 * retry limits, and default secrets, that provide sensible defaults for common client operations.</p>
 */
public final class Defaults {

    private Defaults() {
    }

    /**
     * The buffer duration added to the client timeout to ensure safe execution time.
     */
    public static final Duration CLIENT_TIMEOUT_BUFFER = Duration.ofSeconds(5);

    /**
     * The default timeout duration for client requests.
     */
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    /**
     * The default secret for local Fauna deployments, used in development environments.
     */
    public static final String LOCAL_FAUNA_SECRET = "secret";

    /**
     * The maximum number of retries allowed for handling contention errors.
     */
    public static final int MAX_CONTENTION_RETRIES = 3;
}
