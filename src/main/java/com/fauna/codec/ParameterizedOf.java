package com.fauna.codec;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * A utility class that implements {@link ParameterizedType} to represent a type with specified
 * type arguments at runtime.
 *
 * @param <T> The type parameter of the parameterized type.
 */
public class ParameterizedOf<T> implements ParameterizedType {

    private final Type rawType;
    private final Type[] typeArguments;

    /**
     * Constructs a new {@code ParameterizedOf} instance.
     *
     * @param rawType       The raw type (e.g., {@code List.class} for {@code List<String>}).
     * @param typeArguments The type arguments (e.g., {@code String.class} for {@code List<String>}).
     */
    public ParameterizedOf(final Type rawType, final Type[] typeArguments) {
        this.rawType = rawType;
        this.typeArguments = typeArguments;
    }

    /**
     * Returns the type arguments for this parameterized type.
     *
     * @return An array of {@link Type} objects representing the actual type arguments.
     */
    @Override
    public Type[] getActualTypeArguments() {
        return typeArguments;
    }

    /**
     * Returns the raw type of this parameterized type.
     *
     * @return The raw {@link Type} representing the parameterized type.
     */
    @Override
    public Type getRawType() {
        return rawType;
    }

    /**
     * Returns the owner type of this parameterized type.
     *
     * @return {@code null} as this implementation does not support owner types.
     */
    @Override
    public Type getOwnerType() {
        return null;
    }
}
