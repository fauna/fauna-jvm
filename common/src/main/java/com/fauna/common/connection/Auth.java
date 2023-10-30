package com.fauna.common.connection;

class Auth {

    private final String secret;

    public Auth(String secret) {
        this.secret = secret;
    }

    public String bearer() {
        return "Bearer " + secret;
    }
}
