package com.fauna.codec;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * MapOf stores the generic parameter class to evade type erasure during decoding.
 * @param <E> The value class of the Map.
 */
public class MapOf<E> implements ParameterizedOf<Map<String,E>> {
    private final Class<E> valueClass;

    public MapOf(Class<E> valueClass) {
        this.valueClass = valueClass;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return new Type[]{valueClass};
    }

    @Override
    public Type getRawType() {
        return Map.class;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }
}