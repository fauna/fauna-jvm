package com.fauna.client;

import java.time.Duration;
import java.util.Optional;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

import static com.fauna.constants.Defaults.CLIENT_TIMEOUT_BUFFER;
import static com.fauna.constants.Defaults.LOCAL_FAUNA_SECRET;
import static com.fauna.constants.Defaults.MAX_CONTENTION_RETRIES;

/**
 * FaunaConfig is a configuration class used to set up and configure a connection to Fauna.
 * It encapsulates various settings such as the endpoint URL, secret key, and more.
 */
public final class FaunaConfig {

    public static class FaunaEndpoint {
        public static final String DEFAULT = "https://db.fauna.com";
        public static final String LOCAL = "http://localhost:8443";
    }

    private final String endpoint;
    private final String secret;
    private final int maxContentionRetries;
    private final Duration clientTimeoutBuffer;
    private final Handler logHandler;
    private final StatsCollector statsCollector;
    public static final FaunaConfig DEFAULT = FaunaConfig.builder().build();
    public static final FaunaConfig LOCAL = FaunaConfig.builder().endpoint(
            FaunaEndpoint.LOCAL).secret(LOCAL_FAUNA_SECRET).build();

    /**
     * Private constructor for FaunaConfig.
     *
     * @param builder The builder used to create the FaunaConfig instance.
     */
    private FaunaConfig(final Builder builder) {
        this.endpoint = builder.endpoint != null ? builder.endpoint : FaunaEndpoint.DEFAULT;
        this.secret = builder.secret != null ? builder.secret : "";
        this.maxContentionRetries = builder.maxContentionRetries;
        this.clientTimeoutBuffer = builder.clientTimeoutBuffer;
        this.logHandler = builder.logHandler;
        this.statsCollector = builder.statsCollector;
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
     *
     * @return An integer value.
     */
    public int getMaxContentionRetries() {
        return maxContentionRetries;
    }

    /**
     * Gets the buffer that will be added to the HTTP client timeout, in addition to any query timeout.
     *
     * @return The timeout buffer Duration.
     */
    public Duration getClientTimeoutBuffer() {
        return clientTimeoutBuffer;
    }

    /**
     * Gets the log handler that the client will use.
     *
     * @return A log handler instance.
     */
    public Handler getLogHandler() {
        return logHandler;
    }

    /**
     * Gets the stats collector for the client.
     *
     * @return A StatsCollector instance.
     */
    public StatsCollector getStatsCollector() {
        return statsCollector;
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
        private String endpoint =
                FaunaEnvironment.faunaEndpoint().orElse(FaunaEndpoint.DEFAULT);
        private String secret = FaunaEnvironment.faunaSecret().orElse("");
        private int maxContentionRetries = MAX_CONTENTION_RETRIES;
        private Duration clientTimeoutBuffer = CLIENT_TIMEOUT_BUFFER;
        private Handler logHandler = defaultLogHandler();
        private StatsCollector statsCollector = new StatsCollectorImpl();

        static Level getLogLevel(final String debug) {
            if (debug == null || debug.isBlank()) {
                return Level.WARNING;
            } else {
                try {
                    int debugInt = Integer.parseInt(debug);
                    return debugInt > 0 ? Level.FINE : Level.WARNING;
                } catch (NumberFormatException e) {
                    return Level.FINE;
                }
            }
        }

        private static Handler defaultLogHandler() {
            Handler logHandler = new ConsoleHandler();
            logHandler.setLevel(
                    getLogLevel(FaunaEnvironment.faunaDebug().orElse(null)));
            return logHandler;
        }

        /**
         * Sets the endpoint URL.
         *
         * @param endpoint A String representing the endpoint URL.
         * @return The current Builder instance.
         */
        public Builder endpoint(final String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        /**
         * Sets the secret key.
         *
         * @param secret A String representing the secret key.
         * @return The current Builder instance.
         */
        public Builder secret(final String secret) {
            this.secret = secret;
            return this;
        }

        /**
         * Set the Fauna max-contention-retries setting.
         *
         * @param maxContentionRetries A positive integer value.
         * @return The current Builder instance.
         */
        public Builder maxContentionRetries(final int maxContentionRetries) {
            this.maxContentionRetries = maxContentionRetries;
            return this;
        }

        /**
         * Set the client timeout buffer.
         *
         * @param duration The timeout buffer duration.
         * @return The current Builder instance.
         */
        public Builder clientTimeoutBuffer(final Duration duration) {
            this.clientTimeoutBuffer = duration;
            return this;
        }

        /**
         * Override the default log handler with the given log handler.
         *
         * @param handler A log handler instance.
         * @return The current Builder instance.
         */
        public Builder logHandler(final Handler handler) {
            this.logHandler = handler;
            return this;
        }

        /**
         * Set a StatsCollector.
         *
         * @param statsCollector A stats collector instance.
         * @return The current Builder instance.
         */
        public Builder statsCollector(final StatsCollector statsCollector) {
            this.statsCollector = statsCollector;
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

        private static Optional<String> environmentVariable(final String name) {
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
