package com.fauna.codec;

/**
 * A helper class for providing static access to parameterized generic types, aiding in
 * deserialization by circumventing type erasure.
 */
public final class Generic {

    /**
     * Private constructor to prevent instantiation.
     */
    private Generic() {
    }

    /**
     * Creates a {@link ListOf} instance for the specified element type.
     *
     * @param elementClass The class of the elements contained in the list.
     * @param <E>          The type of elements in the list.
     * @return A {@link ListOf} instance with the specified element type.
     */
    public static <E> ListOf<E> listOf(final Class<E> elementClass) {
        return new ListOf<>(elementClass);
    }

    /**
     * Creates a {@link MapOf} instance for a map with {@link String} keys and the specified value type.
     *
     * @param valueClass The class of the map's values.
     * @param <K>        The type of keys in the map (constrained to {@link String}).
     * @param <V>        The type of values in the map.
     * @return A {@link MapOf} instance with {@link String} keys and the specified value type.
     */
    public static <K extends String, V> MapOf<K, V> mapOf(final Class<V> valueClass) {
        return new MapOf<>(valueClass);
    }

    /**
     * Creates a {@link PageOf} instance for the specified element type.
     *
     * @param valueClass The class of the elements contained in the page.
     * @param <E>        The type of elements in the page.
     * @return A {@link PageOf} instance with the specified element type.
     */
    public static <E> PageOf<E> pageOf(final Class<E> valueClass) {
        return new PageOf<>(valueClass);
    }

    /**
     * Creates an {@link OptionalOf} instance for the specified element type.
     *
     * @param valueClass The class of the elements contained in the optional.
     * @param <E>        The type of the element in the optional.
     * @return An {@link OptionalOf} instance with the specified element type.
     */
    public static <E> OptionalOf<E> optionalOf(final Class<E> valueClass) {
        return new OptionalOf<>(valueClass);
    }

    /**
     * Creates a {@link NullableDocumentOf} instance for the specified element type.
     *
     * @param valueClass The class of the elements contained in the nullable document.
     * @param <E>        The type of the element in the nullable document.
     * @return A {@link NullableDocumentOf} instance with the specified element type.
     */
    public static <E> NullableDocumentOf<E> nullableDocumentOf(final Class<E> valueClass) {
        return new NullableDocumentOf<>(valueClass);
    }
}
