package com.fauna.client;

import java.util.Optional;

public class FaunaScope {
    private static final String DELIMITER = ":";
    private final String tenantDatabase;
    private final FaunaRole role;

    public FaunaScope(String tenantDatabase, FaunaRole role) {
        this.tenantDatabase = tenantDatabase;
        this.role = role;
    }

    public FaunaScope(Builder builder) {
        this.tenantDatabase = builder.tenantDatabase;;
        this.role = builder.role.orElse(FaunaRole.SERVER);
    }

    public String getToken(String secret) {
        return String.join(DELIMITER, secret, tenantDatabase, role.toString());
    }

    public static class Builder {
        public final String tenantDatabase;
        public Optional<FaunaRole> role = Optional.empty();

        public Builder(String tenantDatabase) {
            this.tenantDatabase = tenantDatabase;
        }

        public Builder withRole(FaunaRole role) {
            this.role = Optional.ofNullable(role);
            return this;
        }

        public FaunaScope build() {
            return new FaunaScope(this.tenantDatabase, this.role.orElse(FaunaRole.SERVER));

        }
    }

    public static Builder builder(String tenantDatabase) {
        return new Builder(tenantDatabase);
    }



}
