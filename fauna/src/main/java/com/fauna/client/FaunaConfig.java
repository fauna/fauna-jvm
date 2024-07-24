package com.fauna.client;

import com.fauna.query.QueryOptions;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


/**
 * FaunaConfig is a configuration class used to set up and configure a connection to Fauna.
 * It encapsulates various settings such as the endpoint URL, secret key, query timeout, and others.
 */
public class FaunaConfig {

    public static class FaunaEndpoint {
        public static String DEFAULT = "https://db.fauna.com";
        public static String LOCAL = "https://localhost:8443";
    }

    private final String endpoint;
    private final String secret;

//    private Integer maxContentionRetries;
//    private int maxAttempts;
//    private int maxBackoff;


    /**
     * Private constructor for FaunaConfig.
     *
     * @param builder The builder used to create the FaunaConfig instance.
     */
    private FaunaConfig(Builder builder) {
        this.endpoint = builder.endpoint.orElseGet(() -> FaunaEnvironment.faunaEndpoint().orElse(FaunaEndpoint.DEFAULT));
        this.secret = builder.secret.orElseGet(() -> FaunaEnvironment.faunaSecret().orElse(""));
    }

    /**
     * Gets the Fauna endpoint URL.
     *
     * @return A String representing the endpoint URL.
     * The default is <a href="https://db.fauna.com">https://db.fauna.com</a>
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Gets the secret key used for authentication.
     *
     * @return A String representing the secret key.
     */
    public String getSecret() {
        return secret;
    }

    /**
     * Creates a new builder for FaunaConfig.
     *
     * @return A new instance of Builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for FaunaConfig. Follows the Builder Design Pattern.
     */
    public static class Builder {
        private Optional<String> endpoint = Optional.empty();
        private Optional<String> secret = Optional.empty();

        /**
         * Sets the endpoint URL.
         *
         * @param endpoint A String representing the endpoint URL.
         * @return The current Builder instance.
         */
        public Builder endpoint(String endpoint) {
            this.endpoint = Optional.ofNullable(endpoint);
            return this;
        }

        /**
         * Sets the secret key.
         *
         * @param secret A String representing the secret key.
         * @return The current Builder instance.
         */
        public Builder secret(String secret) {
            this.secret = Optional.ofNullable(secret);
            return this;
        }

        /**
         * Builds and returns a new FaunaConfig instance.
         *
         * @return A new instance of FaunaConfig.
         */
        public FaunaConfig build() {
            return new FaunaConfig(this);
        }
    }

    /**
     * This class handles reading Fauna environment variables for the client.
     */
    public static class FaunaEnvironment {
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
}
