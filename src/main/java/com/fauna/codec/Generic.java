package com.fauna.codec;

/**
 * A helper class for static access to parameterized generics for deserialization.
 */
public class Generic {

    public static <E> ListOf<E> listOf(Class<E> elementClass) {
        return new ListOf<>(elementClass);
    }

    public static <K extends String, V> MapOf<K, V> mapOf(Class<V> valueClass) {
        return new MapOf<>(valueClass);
    }

    public static <E> PageOf<E> pageOf(Class<E> valueClass) {
        return new PageOf<>(valueClass);
    }

    public static <E> OptionalOf<E> optionalOf(Class<E> valueClass) {
        return new OptionalOf<>(valueClass);
    }

    public static <E> NullableDocumentOf<E> nullableDocumentOf(
            Class<E> valueClass) {
        return new NullableDocumentOf<>(valueClass);
    }
}
