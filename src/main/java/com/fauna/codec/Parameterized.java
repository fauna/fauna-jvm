package com.fauna.codec;

/**
 * A helper class for static access to parameterized generics for decoding.
 */
public class Parameterized {

    public static <T> ListOf<T> listOf(Class<T> clazz) {
        return new ListOf<>(clazz);
    }

    public static <T> MapOf<T> mapOf(Class<T> clazz) {
        return new MapOf<>(clazz);
    }

    public static <T> PageOf<T> pageOf(Class<T> clazz) {
        return new PageOf<>(clazz);
    }

    public static <T> OptionalOf<T> optionalOf(Class<T> clazz) {
        return new OptionalOf<>(clazz);
    }
}
