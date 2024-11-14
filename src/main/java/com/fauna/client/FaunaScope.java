package com.fauna.client;

/**
 * Represents a FaunaScope, a structure that encapsulates a Fauna database and a role within that database.
 * The FaunaScope is used to generate a token that is used for authorization.
 */
public final class FaunaScope {
    private static final String DELIMITER = ":";
    private final String database;
    private final FaunaRole role;

    /**
     * Creates a FaunaScope with the specified database and role.
     *
     * @param database the name of the database
     * @param role the FaunaRole associated with this scope
     */
    public FaunaScope(final String database, final FaunaRole role) {
        this.database = database;
        this.role = role;
    }

    /**
     * Generates a token for this scope using the provided secret.
     *
     * @param secret the secret used to generate the token
     * @return a token string formed by concatenating secret, database, and role
     */
    public String getToken(final String secret) {
        return String.join(DELIMITER, secret, database, role.toString());
    }

    /**
     * A builder class for creating instances of FaunaScope.
     */
    public static class Builder {
        private final String database;
        private FaunaRole role = null;

        /**
         * Constructs a Builder for FaunaScope.
         *
         * @param database the name of the database
         */
        public Builder(final String database) {
            this.database = database;
        }

        /**
         * Sets the role for the FaunaScope.
         *
         * @param role the FaunaRole to associate with the scope
         * @return the Builder instance for method chaining
         */
        public Builder withRole(final FaunaRole role) {
            this.role = role;
            return this;
        }

        /**
         * Builds a FaunaScope instance using the current builder settings.
         *
         * @return a newly created FaunaScope
         */
        public FaunaScope build() {
            return new FaunaScope(this.database,
                    this.role != null ? this.role : FaunaRole.SERVER);
        }
    }

    /**
     * Creates a new Builder instance for a FaunaScope.
     *
     * @param database the name of the database
     * @return a new Builder instance
     */
    public static Builder builder(final String database) {
        return new Builder(database);
    }
}
