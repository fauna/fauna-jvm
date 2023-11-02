package com.fauna.common.connection;

/**
 * The Auth class is responsible for handling authentication, providing functionality to generate bearer tokens.
 */
class Auth {

    private final String secret;

    /**
     * Constructs a new Auth instance with the provided secret.
     *
     * @param secret The secret key used for authentication.
     */
    public Auth(String secret) {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalArgumentException("Secret cannot be null or empty");
        }
        this.secret = secret;
    }

    /**
     * Generates a bearer token using the secret provided during construction.
     *
     * @return A string representing the bearer token.
     */
    public String bearer() {
        return "Bearer " + secret;
    }
}
