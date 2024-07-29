package com.fauna.client;

import java.util.Optional;

public class FaunaScope {
    private static final String DELIMITER = ":";
    private final String database;
    private final FaunaRole role;

    public FaunaScope(String database, FaunaRole role) {
        this.database = database;
        this.role = role;
    }

    public String getToken(String secret) {
        return String.join(DELIMITER, secret, database, role.toString());
    }

    public static class Builder {
        public final String database;
        public Optional<FaunaRole> role = Optional.empty();

        public Builder(String database) {
            this.database = database;
        }

        public Builder withRole(FaunaRole role) {
            this.role = Optional.ofNullable(role);
            return this;
        }

        public FaunaScope build() {
            return new FaunaScope(this.database, this.role.orElse(FaunaRole.SERVER));

        }
    }

    public static Builder builder(String database) {
        return new Builder(database);
    }



}
