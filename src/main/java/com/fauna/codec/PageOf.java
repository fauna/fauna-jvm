package com.fauna.codec;

import com.fauna.types.Page;
import java.lang.reflect.Type;

/**
 * PageOf stores the generic parameter class to evade type erasure during deserialization.
 * @param <E> The element class of the page.
 */
public class PageOf<E> implements ParameterizedOf<Page<E>> {
    private final Class<E> clazz;

    public PageOf(Class<E> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return new Type[]{clazz};
    }

    @Override
    public Type getRawType() {
        return Page.class;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }
}