package com.fauna.codec;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ParameterizedOf<T> implements ParameterizedType {
    private final Type rawType;
    private final Type[] typeArguments;

    public ParameterizedOf(Type rawType, Type[] typeArguments) {

        this.rawType = rawType;
        this.typeArguments = typeArguments;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return typeArguments;
    }

    @Override
    public Type getRawType() {
        return rawType;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }
}
