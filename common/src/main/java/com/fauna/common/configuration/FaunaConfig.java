package com.fauna.common.configuration;

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


    private final String endpoint;
    private final String secret;
    private final Boolean linearized;
    private final Boolean typeCheck;
    private final Duration queryTimeout;
    private final String traceParent;
    private final Map<String, String> queryTags;

//    private Integer maxContentionRetries;
//    private int maxAttempts;
//    private int maxBackoff;

    private static final Duration DEFAULT_QUERY_TIMEOUT = Duration.ofSeconds(5);

    /**
     * Private constructor for FaunaConfig.
     *
     * @param builder The builder used to create the FaunaConfig instance.
     */
    private FaunaConfig(Builder builder) {
        this.endpoint = FaunaEnvironment.faunaEndpoint().orElse(builder.endpoint);
        this.secret = FaunaEnvironment.faunaSecret().orElse(builder.secret);
        this.queryTimeout = builder.queryTimeout;
        this.linearized = builder.linearized;
        this.typeCheck = builder.typeCheck;
        this.queryTags = builder.queryTags;
        this.traceParent = builder.traceParent;
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
     * Gets the query timeout setting.
     *
     * @return A Duration representing the maximum amount of time Fauna will execute the query before marking it failed.
     * The default is 5 sec
     */
    public Duration getQueryTimeout() {
        return queryTimeout;
    }

    /**
     * Gets the linearized setting.
     *
     * @return A Boolean indicating whether to unconditionally run the query as strictly serialized.
     */
    public Optional<Boolean> getLinearized() {
        return Optional.ofNullable(this.linearized);
    }

    /**
     * Gets the type check setting.
     *
     * @return A Boolean indicating whether to enable or disable type checking of the query before evaluation.
     */
    public Optional<Boolean> getTypeCheck() {
        return Optional.ofNullable(this.typeCheck);
    }

    /**
     * Gets the query tags.
     *
     * @return A Map of Strings representing tags associated with the query.
     */
    public Map<String, String> getQueryTags() {
        return Objects.requireNonNullElseGet(this.queryTags, HashMap::new);
    }

    /**
     * Gets the trace parent setting.
     *
     * @return A String representing a traceparent associated with the query.
     */
    public Optional<String> getTraceParent() {
        return Optional.ofNullable(this.traceParent);
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
        private String endpoint = Endpoint.DEFAULT.toString();
        private String secret = "";
        private Duration queryTimeout = DEFAULT_QUERY_TIMEOUT;
        private Boolean linearized = null;
        private Boolean typeCheck = null;
        private Map<String, String> queryTags = null;
        private String traceParent = null;

        /**
         * Sets the endpoint URL.
         *
         * @param endpoint A String representing the endpoint URL.
         * @return The current Builder instance.
         */
        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        /**
         * Sets the secret key.
         *
         * @param secret A String representing the secret key.
         * @return The current Builder instance.
         */
        public Builder secret(String secret) {
            this.secret = secret;
            return this;
        }

        /**
         * Sets the query timeout.
         *
         * @param queryTimeout A Duration representing the query timeout.
         *                     Controls the maximum amount of time Fauna will execute your query before marking it failed.
         * @return The current Builder instance.
         */
        public Builder queryTimeout(Duration queryTimeout) {
            this.queryTimeout = queryTimeout;
            return this;
        }

        /**
         * Sets the linearized setting.
         *
         * @param linearized A Boolean indicating whether to run the query as strictly serialized.
         *                   This affects read-only transactions.
         *                   Transactions which write will always be strictly serialized.
         * @return The current Builder instance.
         */
        public Builder linearized(Boolean linearized) {
            this.linearized = linearized;
            return this;
        }

        /**
         * Sets the type check setting.
         *
         * @param typeCheck A Boolean indicating whether to enable or disable type checking of the query.
         *                  If not set, the value configured on the Client will be used.
         *                  If neither is set, Fauna will use the value of the "typechecked" flag on the database configuration.
         * @return The current Builder instance.
         */
        public Builder typeCheck(Boolean typeCheck) {
            this.typeCheck = typeCheck;
            return this;
        }

        /**
         * Sets the query tags.
         *
         * @param queryTags A Map of Strings representing the query tags.
         *                  Tags to associate with the query. See `logging <<a href="https://docs.fauna.com/fauna/current/build/logs/query_log/">...</a>>`_
         * @return The current Builder instance.
         */
        public Builder queryTags(Map<String, String> queryTags) {
            this.queryTags = queryTags;
            return this;
        }

        /**
         * Sets the trace parent.
         *
         * @param traceParent A String representing the traceparent associated with the query.
         *                    See `logging <<a href="https://docs.fauna.com/fauna/current/build/logs/query_log/">...</a>>`_ Must match format: <a href="https://www.w3.org/TR/trace-context/#traceparent-header">...</a>
         * @return The current Builder instance.
         */
        public Builder traceParent(String traceParent) {
            this.traceParent = traceParent;
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

}
