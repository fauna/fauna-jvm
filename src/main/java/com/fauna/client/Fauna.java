package com.fauna.client;

import java.net.http.HttpClient;

public class Fauna {

    /**
     * Create a default Fauna client.
     * @return          A FaunaClient (or subclass of it).
     */
    public static FaunaClient client() {
        return new BaseFaunaClient(FaunaConfig.builder().build());
    }

    /**
     * Create a Fauna client with the given FaunaConfig (and default HTTP client, and RetryStrategy).
     * @param config    Fauna configuration object.
     * @return          A FaunaClient (or subclass of it).
     */
    public static FaunaClient client(FaunaConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("FaunaConfig cannot be null.");
        }
        return new BaseFaunaClient(config);
    }

    /**
     * Create a Fauna client with the given FaunaConfig, HTTP client, and RetryStrategy.
     * @param config        Fauna configuration object.
     * @param httpClient    A HTTP client (from java.net.http in Java 11+).
     * @param retryStrategy An implementation of RetryStrategy.
     * @return              A FaunaClient (or subclass of it).
     */
    public static FaunaClient client(FaunaConfig config, HttpClient httpClient, RetryStrategy retryStrategy) {
        if (config == null) {
            throw new IllegalArgumentException("FaunaConfig cannot be null.");
        }
        return new BaseFaunaClient(config, httpClient, retryStrategy);
    }

    /**
     * Create a new Fauna client that wraps an existing client, but is scoped to a specific tenant database.
     * @param client            Another Fauna client.
     * @param tenantDatabase    The name of the tenant database.
     * @return                  A FaunaClient (or subclass of it).
     */
    public static FaunaClient scoped(FaunaClient client, String tenantDatabase) {
        if (client == null) {
            throw new IllegalArgumentException("FaunaClient cannot be null.");
        }
        if (tenantDatabase == null || tenantDatabase.isEmpty()) {
            throw new IllegalArgumentException("tenantDatabase cannot be null or empty.");
        }
        return new ScopedFaunaClient(client, FaunaScope.builder(tenantDatabase).build());
    }

    /**
     * Create a new Fauna client that wraps an existing client, but is scoped to a specific tenant database.
     * @param client            Another Fauna client.
     * @param tenantDatabase    The name of the tenant database.
     * @param role              A Fauna role (either built-in or user defined).
     * @return                  A FaunaClient (or subclass of it).
     */
    public static FaunaClient scoped(FaunaClient client, String tenantDatabase, FaunaRole role) {
        if (client == null) {
            throw new IllegalArgumentException("FaunaClient cannot be null.");
        }
        if (tenantDatabase == null || tenantDatabase.isEmpty()) {
            throw new IllegalArgumentException("tenantDatabase cannot be null or empty.");
        }
        if (role == null) {
            throw new IllegalArgumentException("role cannot be null or empty.");
        }
        return new ScopedFaunaClient(client, FaunaScope.builder(tenantDatabase).withRole(role).build());
    }

    /**
     * Create a Fauna client for local development using the Fauna Docker container.
     * @return                  A FaunaClient (or subclass of it).
     */
    public static FaunaClient local() {
        return new BaseFaunaClient(FaunaConfig.LOCAL);
    }
}
