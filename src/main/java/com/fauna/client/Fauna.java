package com.fauna.client;

import java.net.http.HttpClient;

public final class Fauna {

    private Fauna() {
    }

    /**
     * Create a default Fauna client.
     *
     * @return A FaunaClient (or subclass of it).
     */
    public static FaunaClient client() {
        return new BaseFaunaClient(FaunaConfig.builder().build());
    }

    /**
     * Create a Fauna client with the given FaunaConfig (and default HTTP client, and RetryStrategy).
     *
     * @param config Fauna configuration object.
     * @return A FaunaClient (or subclass of it).
     */
    public static FaunaClient client(final FaunaConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("FaunaConfig cannot be null.");
        }
        return new BaseFaunaClient(config);
    }

    /**
     * Create a Fauna client with the given FaunaConfig, HTTP client, and RetryStrategy.
     *
     * @param config        Fauna configuration object.
     * @param httpClient    An HTTP client (from java.net.http in Java 11+).
     * @param retryStrategy An implementation of RetryStrategy.
     * @return A FaunaClient (or subclass of it).
     */
    public static FaunaClient client(final FaunaConfig config, final HttpClient httpClient,
                                     final RetryStrategy retryStrategy) {
        if (config == null) {
            throw new IllegalArgumentException("FaunaConfig cannot be null.");
        }
        return new BaseFaunaClient(config, httpClient, retryStrategy);
    }

    /**
     * Create a new Fauna client that wraps an existing client, but is scoped to a specific database.
     *
     * @param client   Another Fauna client.
     * @param database The name of the database.
     * @return A FaunaClient (or subclass of it).
     */
    public static FaunaClient scoped(final FaunaClient client, final String database) {
        if (client == null) {
            throw new IllegalArgumentException("FaunaClient cannot be null.");
        }
        if (database == null || database.isEmpty()) {
            throw new IllegalArgumentException(
                    "database cannot be null or empty.");
        }
        return new ScopedFaunaClient(client,
                FaunaScope.builder(database).build());
    }

    /**
     * Create a new Fauna client that wraps an existing client, but is scoped to a specific database.
     *
     * @param client   Another Fauna client.
     * @param database The name of the database.
     * @param role     A Fauna role (either built-in or user defined).
     * @return A FaunaClient (or subclass of it).
     */
    public static FaunaClient scoped(final FaunaClient client, final String database,
                                     final FaunaRole role) {
        if (client == null) {
            throw new IllegalArgumentException("FaunaClient cannot be null.");
        }
        if (database == null || database.isEmpty()) {
            throw new IllegalArgumentException(
                    "database cannot be null or empty.");
        }
        if (role == null) {
            throw new IllegalArgumentException("role cannot be null or empty.");
        }
        return new ScopedFaunaClient(client,
                FaunaScope.builder(database).withRole(role).build());
    }

    /**
     * Create a Fauna client for local development using the Fauna Docker container.
     *
     * @return A FaunaClient (or subclass of it).
     */
    public static FaunaClient local() {
        return new BaseFaunaClient(FaunaConfig.LOCAL);
    }
}
