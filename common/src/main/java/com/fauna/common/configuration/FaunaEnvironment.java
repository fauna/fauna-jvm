package com.fauna.common.configuration;

import java.util.Optional;


/**
 * This class handles reading Fauna environment variables for the client.
 */
public class FaunaEnvironment {
    private static final String FAUNA_SECRET = "FAUNA_SECRET";
    private static final String FAUNA_ENDPOINT = "FAUNA_ENDPOINT";

    private static Optional<String> environmentVariable(String name) {
        Optional<String> var = Optional.ofNullable(System.getenv(name));
        return var.isPresent() && var.get().isBlank() ? Optional.empty() : var;
    }

    /**
     * @return The (non-empty, non-blank) value of the FAUNA_SECRET environment variable, or Optional.empty().
     */
    public static Optional<String> faunaSecret() {
        return environmentVariable(FAUNA_SECRET);
    }

    /**
     * @return The (non-empty, non-blank) value of the FAUNA_ENDPOINT environment variable, or Optional.empty().
     */
    public static Optional<String> faunaEndpoint() {
        return environmentVariable(FAUNA_ENDPOINT);
    }
}
