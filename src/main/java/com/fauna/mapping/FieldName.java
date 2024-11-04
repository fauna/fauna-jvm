package com.fauna.mapping;

public class FieldName {

    public static String canonical(String name) {
        if (name == null || name.isEmpty() ||
                Character.isLowerCase(name.charAt(0))) {
            return name;
        } else {
            return Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }
    }
}
