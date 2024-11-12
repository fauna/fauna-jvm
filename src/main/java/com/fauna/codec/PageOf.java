package com.fauna.codec;

import com.fauna.types.Page;

import java.lang.reflect.Type;

/**
 * Represents a {@link Page} with a specified element type, allowing for retention of the
 * generic type {@code V} during deserialization by circumventing type erasure.
 *
 * @param <V> The element type within the page.
 */
public final class PageOf<V> extends ParameterizedOf<Page<V>> {

    /**
     * Constructs a {@code PageOf} instance for the specified element type.
     *
     * @param valueClass The class of the elements contained in the page.
     */
    public PageOf(final Class<V> valueClass) {
        super(Page.class, new Type[]{valueClass});
    }
}
