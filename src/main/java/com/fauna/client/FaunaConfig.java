package com.fauna.client;

import java.util.Optional;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

import static com.fauna.client.FaunaConfig.FaunaDebug.DEBUG;
import static com.fauna.client.FaunaConfig.FaunaDebug.ERROR;
import static com.fauna.client.FaunaConfig.FaunaDebug.INFO;
import static com.fauna.client.FaunaConfig.FaunaDebug.TRACE;
import static com.fauna.client.FaunaConfig.FaunaDebug.WARNING;


/**
 * FaunaConfig is a configuration class used to set up and configure a connection to Fauna.
 * It encapsulates various settings such as the endpoint URL, secret key, query timeout, and others.
 */
public class FaunaConfig {

    public static class FaunaEndpoint {
        public static final String DEFAULT = "https://db.fauna.com";
        public static final String LOCAL = "http://localhost:8443";
    }

    public static class FaunaDebug {
        public static final String ERROR = "ERROR";
        public static final String WARNING = "WARNING";
        public static final String INFO = "INFO";
        public static final String DEBUG = "DEBUG";
        public static final String TRACE = "TRACE";
    }

    private final String endpoint;
    private final String secret;
    private final int maxContentionRetries;
    private final Handler logHandler;
    public static final FaunaConfig DEFAULT = FaunaConfig.builder().build();
    public static final FaunaConfig LOCAL = FaunaConfig.builder().endpoint(
            FaunaEndpoint.LOCAL).secret("secret").build();

    /**
     * Private constructor for FaunaConfig.
     *
     * @param builder The builder used to create the FaunaConfig instance.
     */
    private FaunaConfig(Builder builder) {
        this.endpoint = builder.endpoint != null ? builder.endpoint : FaunaEndpoint.DEFAULT;
        this.secret = builder.secret != null ? builder.secret : "";
        this.maxContentionRetries = builder.maxContentionRetries;
        this.logHandler = builder.logHandler;
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
     * Gets the number of contention retries that the Fauna server will attempt.
     * @return  An integer value.
     */
    public int getMaxContentionRetries() {
        return maxContentionRetries;
    }

    /**
     * Gets the log handler that the client will use.
     * @return  A log handler instance.
     */
    public Handler getLogHandler() {
        return logHandler;
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
        private String endpoint = FaunaEnvironment.faunaEndpoint().orElse(FaunaEndpoint.DEFAULT);
        private String secret = FaunaEnvironment.faunaSecret().orElse("");
        private int maxContentionRetries = 3;
        private Handler logHandler = defaultLogHandler();

        private static Level getLogLevel(String level) {
            // Map more commonly used log-level names to Java log levels:
            // https://logging.apache.org/log4j/2.x/manual/customloglevels.html
            // https://docs.python.org/3/library/logging.html#logging-levels
            // https://developer.mozilla.org/en-US/docs/Web/API/console/debug_static
            switch (level.toUpperCase()) {
                case ERROR: return Level.SEVERE;
                case INFO: return Level.INFO;
                case DEBUG: return Level.FINE;
                case TRACE: return Level.FINEST;
                case WARNING:
                default: return Level.WARNING;
            }
        }

        private static Handler defaultLogHandler() {
            Handler logHandler = new ConsoleHandler();
            logHandler.setLevel(getLogLevel(FaunaEnvironment.faunaDebug().orElse("")));
            return logHandler;
        }

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
         * Set the Fauna max-contention-retries setting.
         * @param maxContentionRetries  A positive integer value.
         * @return                      The current Builder instance.
         */
        public Builder maxContentionRetries(int maxContentionRetries) {
            this.maxContentionRetries = maxContentionRetries;
            return this;
        }

        /**
         * Override the default log handler with the given log handler.
         * @param handler   A log handler instance.
         * @return          The current Builder instance.
         */
        public Builder logHandler(Handler handler) {
            this.logHandler = handler;
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
        private static final String FAUNA_DEBUG = "FAUNA_DEBUG";

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

        /**
         * @return The (non-empty, non-blank) value of the FAUNA_DEBUG environment variable, or Optional.empty().
         */
        public static Optional<String> faunaDebug() {
            return environmentVariable(FAUNA_DEBUG);
        }
    }
}
