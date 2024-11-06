package com.fauna.client;

import java.text.MessageFormat;
import java.util.Set;

/**
 * Built-in roles defined at:
 * <a href="https://docs.fauna.com/fauna/current/learn/security/roles/">docs.fauna.com</a>.
 */
public class FaunaRole {
    private static final String ADMIN_ROLE_NAME = "admin";
    private static final String SERVER_ROLE_NAME = "server";
    private static final String SERVER_READ_ONLY_ROLE_NAME = "server-readonly";
    private static final Set<String> BUILT_IN_ROLE_NAMES = Set.of(
            ADMIN_ROLE_NAME, SERVER_ROLE_NAME, SERVER_READ_ONLY_ROLE_NAME);

    public static final FaunaRole ADMIN = new FaunaRole(ADMIN_ROLE_NAME);
    public static final FaunaRole SERVER = new FaunaRole(SERVER_ROLE_NAME);
    public static final FaunaRole SERVER_READ_ONLY =
            new FaunaRole(SERVER_READ_ONLY_ROLE_NAME);
    private static final String ROLE_PREFIX = "@role/";
    private static final Character UNDERSCORE = '_';

    private final String role;

    /**
     * Constructor is not public. You should either use one of the built-in roles (ADMIN, SERVER, SERVER_READ_ONLY),
     * or create a user-defined role via Role.named(name).
     *
     * @param role The role name, either @role/name or one of the built-in role names.
     */
    FaunaRole(String role) {
        this.role = role;
    }

    public String toString() {
        return this.role;
    }

    public static void validateRoleName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(
                    "Role name cannot be null or empty.");
        }
        if (BUILT_IN_ROLE_NAMES.contains(name)) {
            String msg = MessageFormat.format(
                    "Role name {0} is reserved, but you can use it as a built-in role",
                    name);
            throw new IllegalArgumentException(msg);
        }
        if (!Character.isAlphabetic(name.charAt(0))) {
            throw new IllegalArgumentException(
                    "First character must be a letter.");
        }
        for (Character c : name.toCharArray()) {
            if (!Character.isAlphabetic(c) && !Character.isDigit(c) &&
                    !c.equals(UNDERSCORE)) {
                throw new IllegalArgumentException(
                        "Role names can only contain letters, numbers, and underscores.");
            }
        }

    }

    public static FaunaRole named(String name) {
        validateRoleName(name);
        return new FaunaRole(ROLE_PREFIX + name);
    }

}
