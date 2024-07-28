package com.fauna.client;

import java.util.Optional;

public class FaunaScope {
    private static final String DELIMITER = ":";
    private static final String ROLE_PREFIX = "@role/";
    private final String tenantDatabase;
    private final String role;

    /**
     * Built-in roles defined at: https://docs.fauna.com/fauna/current/learn/security/roles/
     */
    public static class BuiltIn {
        public static final String ADMIN = "admin";
        public static final String SERVER = "server";
        public static final String SERVER_READ_ONLY = "server-readonly";
    }

    public FaunaScope(String tenantDatabase, String role) {
        this.tenantDatabase = tenantDatabase;
        this.role = role;
    }

    public FaunaScope(Builder builder) {
        this.tenantDatabase = builder.tenantDatabase;;
        this.role = builder.role.orElse(BuiltIn.SERVER);
    }

    public String getToken(String secret) {
        return String.join(DELIMITER, secret, tenantDatabase, ROLE_PREFIX + role);
    }

    public static class Builder {
        public final String tenantDatabase;
        public Optional<String> role = Optional.empty();

        public Builder(String tenantDatabase) {
            this.tenantDatabase = tenantDatabase;
        }

        public Builder withRole(String role) {
            this.role = Optional.ofNullable(role);
            return this;
        }

        public FaunaScope build() {
            return new FaunaScope(this.tenantDatabase, this.role.orElse(BuiltIn.SERVER));

        }
    }

    public static Builder builder(String tenantDatabase) {
        return new Builder(tenantDatabase);
    }



}
