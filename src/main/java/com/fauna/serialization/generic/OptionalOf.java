package com.fauna.serialization.generic;

import java.lang.reflect.Type;
import java.util.Optional;


/**
 * OptionalOf stores the generic parameter class to evade type erasure during deserialization.
 * @param <E> The element class of the list.
 */
public class OptionalOf<E> implements ParameterizedOf<Optional<E>> {

    private final Class<E> clazz;

    public OptionalOf(Class<E> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return new Type[]{clazz};
    }

    @Override
    public Type getRawType() {
        return Optional.class;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }
}