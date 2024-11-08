package com.fauna.mapping;

/**
 * Utility class for handling field names.
 */
public final class FieldName {

    private FieldName() {
    }

    /**
     * Converts the given field name to a canonical format where the first character is lowercase.
     * If the name is null, empty, or already starts with a lowercase letter, it is unchanged.
     *
     * @param name The field name to be converted.
     * @return The canonicalized field name, or the original name if it is null, empty, or already starts with a
     * lowercase letter.
     */
    public static String canonical(final String name) {
        if (name == null || name.isEmpty()
                || Character.isLowerCase(name.charAt(0))) {
            return name;
        } else {
            return Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }
    }
}
