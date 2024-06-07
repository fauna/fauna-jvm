package com.fauna.helper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

public class MapOf<T> implements ParameterizedType {

    private final Class valueClass;

    public MapOf(Class valueClass) {
        this.valueClass = valueClass;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return new Type[]{String.class, valueClass};
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