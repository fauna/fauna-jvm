package com.fauna.codec;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Represents a {@link List} with a specified element type, allowing for retention of the
 * generic type {@code E} during deserialization by circumventing type erasure.
 *
 * @param <E> The type of elements in the list.
 */
public final class ListOf<E> extends ParameterizedOf<List<E>> {

    /**
     * Constructs a {@code ListOf} instance for the specified element type.
     *
     * @param elementClass The class of the elements contained in the list.
     */
    public ListOf(final Class<E> elementClass) {
        super(List.class, new Type[]{elementClass});
    }
}
