package com.fauna.codec;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a unique key in the codec registry.
 */
public class CodecRegistryKey {
    private final Class<?> base;
    private final Type[] typeArgs;

    /**
     * Constructs a new {@code CodecRegistryKey} for the specified class and type arguments.
     *
     * @param clazz    The base class of the codec.
     * @param typeArgs The type arguments for generic types, if applicable.
     * @param <T>      The type of the base class.
     */
    public <T> CodecRegistryKey(final Class<T> clazz, final Type[] typeArgs) {
        this.base = clazz;
        this.typeArgs = typeArgs;
    }

    /**
     * Creates a {@code CodecRegistryKey} for the specified class, without any type arguments.
     *
     * @param clazz The base class of the codec.
     * @param <T>   The type of the base class.
     * @return A new {@code CodecRegistryKey} instance.
     */
    public static <T> CodecRegistryKey from(final Class<T> clazz) {
        return new CodecRegistryKey(clazz, null);
    }

    /**
     * Creates a {@code CodecRegistryKey} for the specified class and type arguments.
     *
     * @param clazz    The base class of the codec.
     * @param typeArgs The type arguments for generic types.
     * @param <T>      The type of the base class.
     * @return A new {@code CodecRegistryKey} instance.
     */
    public static <T> CodecRegistryKey from(final Class<T> clazz, final Type[] typeArgs) {
        return new CodecRegistryKey(clazz, typeArgs);
    }

    /**
     * Compares this key with another object for equality, based on the base class and type arguments.
     *
     * @param other The object to compare with this key.
     * @return {@code true} if the other object is a {@code CodecRegistryKey} with the same base class and type arguments;
     *         {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        } else if (other instanceof CodecRegistryKey) {
            CodecRegistryKey otherCRK = (CodecRegistryKey) other;
            return Objects.equals(base, otherCRK.base) &&
                    Arrays.equals(typeArgs, otherCRK.typeArgs);
        } else {
            return false;
        }
    }

    /**
     * Returns a hash code for this key, based on the base class and type arguments.
     *
     * @return The hash code for this {@code CodecRegistryKey}.
     */
    @Override
    public final int hashCode() {
        return Objects.hash(base, Arrays.hashCode(typeArgs));
    }
}
