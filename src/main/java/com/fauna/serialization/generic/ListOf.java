package com.fauna.serialization.generic;

import java.lang.reflect.Type;
import java.util.List;


/**
 * ListOf stores the generic parameter class to evade type erasure during deserialization.
 * @param <E> The element class of the list.
 */
public class ListOf<E> implements ParameterizedOf<List<E>> {

    private final Class<E> clazz;

    public ListOf(Class<E> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return new Type[]{clazz};
    }

    @Override
    public Type getRawType() {
        return List.class;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }
}